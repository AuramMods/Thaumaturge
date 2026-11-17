package com.leclowndu93150.thaumaturge.serialization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedHashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/** Compiled separately against DFU 8 for the reference and DFU 6 for the port. */
public final class CodecReferenceProbe {
    private final boolean reference;

    public CodecReferenceProbe(boolean reference) { this.reference = reference; }

    private Class<?> factory() {
        try { return reference ? Codec.class : Class.forName("com.leclowndu93150.thaumaturge.serialization.Codecs"); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    @SuppressWarnings("unchecked")
    private <A> A call(String name, Class<?>[] parameters, Object... arguments) {
        try { return (A) factory().getMethod(name, parameters).invoke(null, arguments); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private <A> Codec<A> validate(Codec<A> codec, Function<A, DataResult<A>> validator) {
        if (!reference) return call("validate", new Class<?>[]{Codec.class, Function.class}, codec, validator);
        try { return (Codec<A>) Codec.class.getMethod("validate", Function.class).invoke(codec, validator); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private <A> MapCodec<A> validate(MapCodec<A> codec, Function<A, DataResult<A>> validator) {
        if (!reference) return call("validate", new Class<?>[]{MapCodec.class, Function.class}, codec, validator);
        try { return (MapCodec<A>) MapCodec.class.getMethod("validate", Function.class).invoke(codec, validator); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private <A> Codec<List<A>> list(Codec<A> codec, int min, int max) {
        return call("list", new Class<?>[]{Codec.class, int.class, int.class}, codec, min, max);
    }

    private <A> Codec<A> recursive(String name, Function<Codec<A>, Codec<A>> function) {
        return call("recursive", new Class<?>[]{String.class, Function.class}, name, function);
    }

    private <A> Codec<A> lazy(Supplier<Codec<A>> supplier) {
        return call("lazyInitialized", new Class<?>[]{Supplier.class}, supplier);
    }

    private JsonObject result(DataResult<?> result) {
        JsonObject json = new JsonObject();
        List<String> errors = new ArrayList<>();
        Object partial = result.resultOrPartial(errors::add).orElse(null);
        json.addProperty("success", result.result().isPresent());
        json.addProperty("lifecycle", result.lifecycle().toString());
        json.add("value", value(partial));
        json.addProperty("error", errors.isEmpty() ? null : String.join("; ", errors));
        return json;
    }

    private JsonElement value(Object object) {
        if (object == null) return JsonNull.INSTANCE;
        if (object instanceof JsonElement json) return json;
        if (object instanceof Choice choice) {
            JsonObject json = new JsonObject();
            json.addProperty("kind", choice.kind());
            json.addProperty("n", choice.n());
            return json;
        }
        if (object instanceof Optional<?> optional) {
            JsonObject json = new JsonObject();
            json.addProperty("present", optional.isPresent());
            json.add("value", value(optional.orElse(null)));
            return json;
        }
        if (object instanceof Node node) {
            JsonObject json = new JsonObject();
            json.addProperty("n", node.n());
            json.add("children", value(node.children()));
            return json;
        }
        if (object instanceof List<?> list) {
            JsonArray json = new JsonArray();
            list.forEach(entry -> json.add(value(entry)));
            return json;
        }
        if (object instanceof Pair<?, ?> pair) {
            JsonObject json = new JsonObject();
            json.add("value", value(pair.getFirst()));
            json.add("remainder", value(pair.getSecond()));
            return json;
        }
        return new GsonBuilder().create().toJsonTree(object);
    }

    public JsonObject validations() {
        JsonObject out = new JsonObject();
        Codec<Integer> codec = validate(Codec.INT, n -> n < 0
                ? DataResult.error(() -> "negative", 0) : DataResult.success(n, Lifecycle.deprecated(2)));
        for (String input : List.of("-2", "0", "4", "\"invalid\"")) {
            out.add("decode:" + input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        for (int n : new int[]{-2, 0, 4}) out.add("encode:" + n, result(codec.encodeStart(JsonOps.INSTANCE, n)));
        return out;
    }

    public JsonObject mapValidations() {
        JsonObject out = new JsonObject();
        Codec<Integer> codec = validate(Codec.INT.fieldOf("n"), n -> n <= 3
                ? DataResult.success(n) : DataResult.error(() -> "too large")).codec();
        for (String input : List.of("{}", "{\"n\":2}", "{\"n\":9}", "{\"n\":\"bad\"}")) {
            out.add("decode:" + input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        for (int n : new int[]{2, 9}) out.add("encode:" + n, result(codec.encodeStart(JsonOps.INSTANCE, n)));
        return out;
    }

    public JsonObject lists() {
        JsonObject out = new JsonObject();
        for (int[] bounds : List.of(new int[]{0, 0}, new int[]{0, 2}, new int[]{1, 3}, new int[]{3, 3})) {
            for (boolean partial : List.of(false, true)) {
                AtomicInteger decodes = new AtomicInteger();
                AtomicInteger encodes = new AtomicInteger();
                Codec<Integer> element = partial ? validate(Codec.INT, n -> n < 0
                        ? DataResult.error(() -> "negative", 0) : DataResult.success(n)) : Codec.INT;
                Codec<Integer> counted = new Codec<>() {
                    @Override public <T> DataResult<Pair<Integer, T>> decode(DynamicOps<T> ops, T input) {
                        decodes.incrementAndGet(); return element.decode(ops, input);
                    }
                    @Override public <T> DataResult<T> encode(Integer input, DynamicOps<T> ops, T prefix) {
                        encodes.incrementAndGet(); return element.encode(input, ops, prefix);
                    }
                };
                Codec<List<Integer>> codec = list(counted, bounds[0], bounds[1]);
                String prefix = bounds[0] + ":" + bounds[1] + ":" + partial + ":";
                for (String input : List.of("[]", "[1]", "[1,2]", "[1,2,3]", "[1,2,3,4]", "[\"bad\",1,2]",
                        "[1,\"bad\",2,3]", "[-1,2,3,4]", "[\"bad\",\"bad\"]", "7")) {
                    decodes.set(0);
                    JsonObject result = result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input)));
                    result.addProperty("elementDecodes", decodes.get());
                    out.add(prefix + "decode:" + input, result);
                }
                for (List<Integer> input : List.of(List.<Integer>of(), List.of(1), List.of(1, 2, 3), List.of(1, 2, 3, 4), List.of(-1, 2))) {
                    encodes.set(0);
                    JsonObject result = result(codec.encodeStart(JsonOps.INSTANCE, input));
                    result.addProperty("elementEncodes", encodes.get());
                    out.add(prefix + "encode:" + input, result);
                }
            }
        }
        return out;
    }

    public JsonObject strings() {
        JsonObject out = new JsonObject();
        for (int[] bounds : List.of(new int[]{0, 1}, new int[]{1, 3}, new int[]{3, 3})) {
            Codec<String> codec = call("string", new Class<?>[]{int.class, int.class}, bounds[0], bounds[1]);
            for (String input : List.of("", "a", "abc", "abcd", "\uD83D\uDD2E")) {
                String key = bounds[0] + ":" + bounds[1] + ":" + input;
                out.add(key + ":decode", result(codec.decode(JsonOps.INSTANCE, value(input))));
                out.add(key + ":encode", result(codec.encodeStart(JsonOps.INSTANCE, input)));
            }
        }
        return out;
    }

    public JsonObject alternatives() {
        JsonObject out = new JsonObject();
        Codec<Integer> codec = call("withAlternative", new Class<?>[]{Codec.class, Codec.class},
                Codec.INT.fieldOf("current").codec(), Codec.INT.fieldOf("legacy").codec());
        for (String input : List.of("{}", "{\"current\":2}", "{\"legacy\":3}", "{\"current\":2,\"legacy\":3}",
                "{\"current\":\"bad\",\"legacy\":3}", "{\"current\":\"bad\",\"legacy\":\"bad\"}")) {
            out.add(input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        out.add("encode", result(codec.encodeStart(JsonOps.INSTANCE, 4)));
        Codec<List<Integer>> converted = call("withAlternative", new Class<?>[]{Codec.class, Codec.class, Function.class},
                Codec.INT.listOf(), Codec.INT, (Function<Integer, List<Integer>>) List::of);
        for (String input : List.of("3", "[1,2]", "[1,\"bad\"]", "\"bad\"")) {
            out.add("converted:" + input, result(converted.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        out.add("converted:encode", result(converted.encodeStart(JsonOps.INSTANCE, List.of(5))));
        return out;
    }

    public JsonObject lazyCodecs() {
        JsonObject out = new JsonObject();
        AtomicInteger builds = new AtomicInteger();
        Codec<Integer> codec = lazy(() -> { builds.incrementAndGet(); return Codec.INT; });
        codec.toString();
        out.addProperty("before", builds.get());
        out.add("decode", result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString("2"))));
        out.add("encode", result(codec.encodeStart(JsonOps.INSTANCE, 3)));
        out.addProperty("after", builds.get());
        AtomicInteger attempts = new AtomicInteger();
        Codec<Integer> retry = lazy(() -> {
            if (attempts.incrementAndGet() == 1) throw new IllegalStateException("fixture");
            return Codec.INT;
        });
        try { retry.parse(JsonOps.INSTANCE, value(1)); throw new AssertionError("expected first factory failure"); }
        catch (IllegalStateException expected) { out.addProperty("firstFailure", expected.getMessage()); }
        out.add("retry", result(retry.parse(JsonOps.INSTANCE, value(1))));
        out.addProperty("attempts", attempts.get());
        return out;
    }

    private record Node(int n, List<Node> children) {}

    public JsonObject recursiveCodecs() {
        JsonObject out = new JsonObject();
        AtomicInteger builds = new AtomicInteger();
        Codec<Node> codec = recursive("fixture", self -> {
            builds.incrementAndGet();
            return RecordCodecBuilder.create(i -> i.group(Codec.INT.fieldOf("n").forGetter(Node::n),
                    list(self, 0, 2).fieldOf("children").forGetter(Node::children)).apply(i, Node::new));
        });
        out.addProperty("before", builds.get());
        for (String input : List.of("{\"n\":1,\"children\":[]}",
                "{\"n\":1,\"children\":[{\"n\":2,\"children\":[]}]}",
                "{\"n\":1,\"children\":[{\"n\":2,\"children\":[]},{\"n\":3,\"children\":[]},{\"n\":4,\"children\":[]}]}",
                "{\"n\":1,\"children\":[{\"n\":\"bad\",\"children\":[]}]}")) {
            out.add(input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        out.add("encode", result(codec.encodeStart(JsonOps.INSTANCE, new Node(1, List.of(new Node(2, List.of()))))));
        out.addProperty("after", builds.get());
        return out;
    }

    private MapCodec<?> optional(Codec<Integer> element, boolean lenient, boolean defaults) {
        String method = lenient ? "lenientOptionalFieldOf" : "optionalFieldOf";
        if (!reference) return defaults ? call(method, new Class<?>[]{Codec.class, String.class, Object.class}, element, "field", 7)
                : call(method, new Class<?>[]{Codec.class, String.class}, element, "field");
        try {
            return defaults ? (MapCodec<?>) Codec.class.getMethod(method, String.class, Object.class).invoke(element, "field", 7)
                    : (MapCodec<?>) Codec.class.getMethod(method, String.class).invoke(element, "field");
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonObject optionalFields() {
        JsonObject out = new JsonObject();
        for (boolean lenient : List.of(false, true)) {
            for (boolean defaults : List.of(false, true)) {
                for (boolean partial : List.of(false, true)) {
                    Codec<Integer> element = validate(Codec.INT, n -> n >= 0 ? DataResult.success(n, Lifecycle.deprecated(1))
                            : partial ? DataResult.error(() -> "negative", 0) : DataResult.error(() -> "negative"));
                    Codec codec = optional(element, lenient, defaults).codec();
                    String prefix = lenient + ":" + defaults + ":" + partial + ":";
                    for (String input : List.of("{}", "{\"field\":2}", "{\"field\":-2}", "{\"field\":\"bad\"}",
                            "{\"field\":null}", "\"bad\"")) {
                        out.add(prefix + "decode:" + input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
                    }
                    for (int n : new int[]{2, 7, -2}) out.add(prefix + "encode:" + n,
                            result(codec.encodeStart(JsonOps.INSTANCE, defaults ? n : Optional.of(n))));
                    if (!defaults) out.add(prefix + "encode:empty", result(codec.encodeStart(JsonOps.INSTANCE, Optional.empty())));
                    for (String input : List.of("[null]", "[2]", "[-2]", "[\"bad\"]")) {
                        out.add(prefix + "compressed:" + input, result(codec.decode(JsonOps.COMPRESSED, JsonParser.parseString(input))));
                    }
                }
            }
        }
        return out;
    }

    private record Choice(String kind, int n) {}

    private <A> Codec<A> dispatch(Codec<String> keys, String name, Function<A, String> type,
                                 Function<String, MapCodec<? extends A>> formats) {
        if (!reference) return call("dispatch", new Class<?>[]{Codec.class, String.class, Function.class, Function.class},
                keys, name, type, formats);
        try { return (Codec<A>) Codec.class.getMethod("dispatch", String.class, Function.class, Function.class)
                .invoke(keys, name, type, formats); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    private <A> Codec<A> partialDispatch(Codec<String> keys, String name, Function<A, DataResult<String>> type,
                                        Function<String, DataResult<? extends MapCodec<? extends A>>> formats) {
        if (!reference) return call("partialDispatch", new Class<?>[]{Codec.class, String.class, Function.class, Function.class},
                keys, name, type, formats);
        try { return (Codec<A>) Codec.class.getMethod("partialDispatch", String.class, Function.class, Function.class)
                .invoke(keys, name, type, formats); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    public JsonObject dispatchedSchemas(boolean partial) {
        JsonObject out = new JsonObject();
        MapCodec<Choice> a = Codec.INT.fieldOf("first").xmap(n -> new Choice("a", n), Choice::n);
        MapCodec<Choice> b = validate(Codec.INT, n -> n >= 0 ? DataResult.success(n)
                : DataResult.error(() -> "negative", 0)).fieldOf("second").xmap(n -> new Choice("b", n), Choice::n);
        Codec<String> keys = validate(Codec.STRING, key -> key.equals("a") || key.equals("b")
                ? DataResult.success(key) : DataResult.error(() -> "Unknown type " + key));
        Codec<Choice> codec = partial ? partialDispatch(Codec.STRING, "format",
                choice -> choice.kind().equals("partialtype") ? DataResult.error(() -> "type partial", "a")
                        : choice.kind().equals("badtype") ? DataResult.error(() -> "bad type") : DataResult.success(choice.kind()),
                key -> switch (key) {
                    case "a" -> DataResult.success(a);
                    case "b" -> DataResult.success(b);
                    case "partialcodec" -> DataResult.error(() -> "codec partial", a);
                    default -> DataResult.error(() -> "Unknown format " + key);
                }) : dispatch(keys, "format", Choice::kind, key -> key.equals("a") ? a : b);
        for (String input : List.of("{}", "{\"format\":\"a\",\"first\":2}", "{\"format\":\"b\",\"second\":3}",
                "{\"format\":\"b\",\"second\":-2}", "{\"format\":\"a\",\"first\":\"bad\"}",
                "{\"format\":\"unknown\",\"first\":2}", "{\"format\":\"partialcodec\",\"first\":2}",
                "{\"format\":3}", "{\"format\":\"a\"}")) {
            out.add("decode:" + input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        for (String input : List.of("[\"a\",[2]]", "[\"b\",[-2]]", "[\"a\"]", "[\"unknown\",[2]]",
                "[\"partialcodec\",[2]]")) {
            try { out.add("compressed:" + input, result(codec.decode(JsonOps.COMPRESSED, JsonParser.parseString(input)))); }
            catch (RuntimeException failure) {
                JsonObject thrown = new JsonObject();
                thrown.addProperty("thrown", failure.getClass().getName());
                out.add("compressed:" + input, thrown);
            }
        }
        for (String kind : List.of("a", "b", "unknown", "badtype", "partialtype", "partialcodec")) {
            for (int n : new int[]{2, -2}) {
                Choice choice = new Choice(kind, n);
                out.add("encode:" + kind + ":" + n, result(codec.encodeStart(JsonOps.INSTANCE, choice)));
                out.add("encodeCompressed:" + kind + ":" + n, result(codec.encodeStart(JsonOps.COMPRESSED, choice)));
            }
        }
        return out;
    }

    public JsonObject throwingResults() {
        JsonObject out = new JsonObject();
        for (boolean partial : List.of(false, true)) {
            DataResult<Integer> value = partial ? DataResult.error(() -> "fixture failure", 5) : DataResult.error(() -> "fixture failure");
            out.add("error:" + partial, invokeThrowing(value));
        }
        out.add("success", invokeThrowing(DataResult.success(7)));
        return out;
    }

    private JsonElement invokeThrowing(DataResult<Integer> input) {
        try {
            Integer result = reference ? (Integer) DataResult.class.getMethod("getOrThrow").invoke(input)
                    : call("getOrThrow", new Class<?>[]{DataResult.class}, input);
            return value(result);
        } catch (InvocationTargetException failure) {
            JsonObject json = new JsonObject();
            json.addProperty("thrown", failure.getCause().getClass().getName());
            json.addProperty("message", failure.getCause().getMessage());
            return json;
        } catch (AssertionError failure) {
            if (reference || !(failure.getCause() instanceof InvocationTargetException wrapped)) throw failure;
            JsonObject json = new JsonObject();
            json.addProperty("thrown", wrapped.getCause().getClass().getName());
            json.addProperty("message", wrapped.getCause().getMessage());
            return json;
        } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
    }

    @SuppressWarnings("unchecked")
    public JsonObject strictMaps() {
        JsonObject out = new JsonObject();
        Codec<String> key = validate(Codec.STRING, text -> text.isBlank() ? DataResult.error(() -> "blank key") : DataResult.success(text));
        Codec<Integer> value = validate(Codec.INT, n -> n < 0 ? DataResult.error(() -> "negative", 0) : DataResult.success(n));
        Codec<Map<String, Integer>> codec;
        try {
            codec = reference ? (Codec<Map<String, Integer>>) Class.forName("axw$b").getConstructor(Codec.class, Codec.class)
                    .newInstance(key, value) : call("strictUnboundedMap", new Class<?>[]{Codec.class, Codec.class}, key, value);
        } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
        for (String input : List.of("{}", "{\"a\":2}", "{\"a\":2,\"b\":3}", "{\"a\":2,\"b\":-1}",
                "{\"\":2}", "{\"\":\"bad\"}", "{\"a\":\"bad\"}", "[]", "7")) {
            out.add("decode:" + input, result(codec.decode(JsonOps.INSTANCE, JsonParser.parseString(input))));
        }
        for (int n : new int[]{2, -1}) {
            Map<String, Integer> input = new LinkedHashMap<>();
            input.put("a", n);
            out.add("encode:" + n, result(codec.encodeStart(JsonOps.INSTANCE, input)));
            out.add("encodeCompressed:" + n, result(codec.encodeStart(JsonOps.COMPRESSED, input)));
        }
        return out;
    }

    public JsonObject all() {
        JsonObject out = new JsonObject();
        out.add("validation", validations());
        out.add("mapValidation", mapValidations());
        out.add("lists", lists());
        out.add("strings", strings());
        out.add("alternatives", alternatives());
        out.add("lazy", lazyCodecs());
        out.add("recursive", recursiveCodecs());
        out.add("optional", optionalFields());
        out.add("dispatch", dispatchedSchemas(false));
        out.add("partialDispatch", dispatchedSchemas(true));
        out.add("throwing", throwingResults());
        return out;
    }

    public static void main(String[] args) throws Exception {
        Files.writeString(Path.of(args[0]), new GsonBuilder().setPrettyPrinting().serializeNulls().create()
                .toJson(args.length > 1 && args[1].equals("strict-map") ? new CodecReferenceProbe(true).strictMaps()
                        : new CodecReferenceProbe(true).all()) + "\n");
    }
}

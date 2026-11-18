package com.leclowndu93150.thaumaturge.compat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Runs the real pinned 1.21.1 packed-color implementation in an isolated reference JVM. */
public final class ColorReferenceProbe {
    private final boolean reference;
    private final Class<?> colors;
    private final Map<String, Method> methods = new HashMap<>();

    public ColorReferenceProbe(boolean reference) throws ClassNotFoundException {
        this.reference = reference;
        colors = Class.forName(reference ? "axy$b" : "com.leclowndu93150.thaumaturge.compat.Colors$ARGB32");
    }

    private Method method(String id) throws NoSuchMethodException {
        if (methods.containsKey(id)) return methods.get(id);
        String mapped = switch (id) {
            case "pack4", "pack3", "withAlpha" -> "color";
            case "fromFloats" -> "colorFromFloat";
            default -> id;
        };
        String obfuscated = switch (id) {
            case "red", "withAlpha" -> "b";
            case "green", "average" -> "c";
            case "blue" -> "d";
            case "opaque" -> "e";
            default -> "a";
        };
        Class<?>[] signature = switch (id) {
            case "pack4" -> new Class<?>[] {int.class, int.class, int.class, int.class};
            case "pack3" -> new Class<?>[] {int.class, int.class, int.class};
            case "withAlpha", "multiply", "average" -> new Class<?>[] {int.class, int.class};
            case "lerp" -> new Class<?>[] {float.class, int.class, int.class};
            case "fromFloats" -> new Class<?>[] {float.class, float.class, float.class, float.class};
            default -> new Class<?>[] {int.class};
        };
        var method = colors.getMethod(reference ? obfuscated : mapped, signature);
        methods.put(id, method);
        return method;
    }

    public int evaluate(JsonObject observation) throws ReflectiveOperationException {
        var method = method(observation.get("method").getAsString());
        var encoded = observation.getAsJsonArray("arguments");
        Object[] values = new Object[encoded.size()];
        for (int i = 0; i < values.length; i++) {
            int bits = encoded.get(i).getAsInt();
            if (method.getParameterTypes()[i] == float.class) values[i] = Float.intBitsToFloat(bits);
            else values[i] = bits;
        }
        return (int) method.invoke(null, values);
    }

    private void observe(JsonArray output, String id, Object... args) throws ReflectiveOperationException {
        var observation = new JsonObject();
        observation.addProperty("method", id);
        var encoded = new JsonArray();
        for (Object arg : args) encoded.add(arg instanceof Float f ? Float.floatToRawIntBits(f) : (Integer) arg);
        observation.add("arguments", encoded);
        observation.addProperty("value", evaluate(observation));
        output.add(observation);
    }

    public JsonArray observations() throws ReflectiveOperationException {
        var out = new JsonArray();
        var rng = new Random(0x7a20c01aL);
        int[] colors = new int[32];
        int[] edges = {0, -1, 1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0xff000000, 0x00ffffff, 0x12345678};
        System.arraycopy(edges, 0, colors, 0, edges.length);
        for (int i = edges.length; i < colors.length; i++) colors[i] = rng.nextInt();
        for (int color : colors) for (String id : List.of("alpha", "red", "green", "blue", "opaque")) observe(out, id, color);
        int[] channels = {-1, 0, 1, 127, 255, 256, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (int a : channels) for (int b : channels) {
            observe(out, "pack4", a, b, 255 - a, b ^ 0x1234);
            observe(out, "pack3", a, b, 255 - a);
            observe(out, "withAlpha", a, b);
        }
        for (int first : colors) for (int second : edges) {
            observe(out, "multiply", first, second);
            observe(out, "average", first, second);
        }
        float[] fractions = {0.0F, -0.0F, 1.0F / 255, 0.5F, 254.0F / 255, 1.0F, -0.1F, 1.1F,
                Float.MIN_VALUE, -Float.MIN_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (int i = 0; i < fractions.length; i++) {
            observe(out, "fromFloats", fractions[i], fractions[(i + 1) % fractions.length],
                    fractions[(i + 2) % fractions.length], fractions[(i + 3) % fractions.length]);
            for (int j = 0; j < colors.length; j++) observe(out, "lerp", fractions[i], colors[j], colors[(j + 1) % colors.length]);
        }
        for (int i = 0; i < 64; i++) {
            observe(out, "fromFloats", rng.nextFloat() * 3 - 1, rng.nextFloat() * 3 - 1,
                    rng.nextFloat() * 3 - 1, rng.nextFloat() * 3 - 1);
        }
        return out;
    }

    public static void main(String[] args) throws Exception {
        var out = new ColorReferenceProbe(true).observations();
        var path = Path.of(args[0]);
        Files.createDirectories(path.getParent());
        Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(out) + "\n");
        System.out.println("Recorded " + out.size() + " actual 1.21.1 color observations");
    }
}

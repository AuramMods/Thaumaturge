package com.leclowndu93150.thaumaturge.api.aspect;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/** Allocates a finite supply to overlapping recipe requirements without counting a unit twice. */
final class AspectAllocation {
    private AspectAllocation() {}

    static AspectRequirements.Allocation allocate(
            List<AspectRequirements.Demand> demands, List<Holder<IAspect>> aspects, AspectList available) {
        int start = 0;
        int aspectStart = 1 + demands.size();
        int end = aspectStart + aspects.size();
        List<List<Edge>> graph = new ArrayList<>(end + 1);
        for (int vertex = 0; vertex <= end; vertex++) graph.add(new ArrayList<>());

        Map<ResourceLocation, Integer> aspectVertices = new HashMap<>();
        List<Edge> supplyEdges = new ArrayList<>(aspects.size());
        for (int index = 0; index < aspects.size(); index++) {
            var aspect = aspects.get(index);
            int vertex = aspectStart + index;
            aspectVertices.put(aspect.unwrapKey().orElseThrow().location(), vertex);
            supplyEdges.add(connect(graph, vertex, end, available.amountOf(aspect)));
        }

        List<Edge> demandEdges = new ArrayList<>(demands.size());
        for (int index = 0; index < demands.size(); index++) {
            var demand = demands.get(index);
            int vertex = 1 + index;
            demandEdges.add(connect(graph, start, vertex, demand.amount()));
            for (var choice : demand.choices()) {
                int aspect = aspectVertices.get(choice.unwrapKey().orElseThrow().location());
                connect(graph, vertex, aspect, demand.amount());
            }
        }

        // A later requirement can move an earlier allocation to another eligible aspect.
        while (augment(graph, start, end)) {
            // Each augmenting path transfers as many units as its lowest remaining capacity.
        }

        AspectList payment = AspectList.EMPTY;
        for (int index = 0; index < aspects.size(); index++) {
            int used = supplyEdges.get(index).reverse.remaining;
            if (used > 0) payment = payment.add(aspects.get(index), used);
        }
        return new AspectRequirements.Allocation(payment,
                demandEdges.stream().map(edge -> edge.reverse.remaining).toList());
    }

    private static Edge connect(List<List<Edge>> graph, int from, int to, int capacity) {
        var forward = new Edge(from, to, capacity);
        var reverse = new Edge(to, from, 0);
        forward.reverse = reverse;
        reverse.reverse = forward;
        graph.get(from).add(forward);
        graph.get(to).add(reverse);
        return forward;
    }

    private static boolean augment(List<List<Edge>> graph, int start, int end) {
        Edge[] previous = new Edge[graph.size()];
        var queue = new ArrayDeque<Integer>();
        queue.add(start);
        while (!queue.isEmpty() && previous[end] == null) {
            for (Edge edge : graph.get(queue.remove())) {
                if (edge.to != start && edge.remaining > 0 && previous[edge.to] == null) {
                    previous[edge.to] = edge;
                    queue.add(edge.to);
                }
            }
        }
        if (previous[end] == null) return false;

        int delta = Integer.MAX_VALUE;
        for (Edge edge = previous[end]; edge != null; edge = previous[edge.from]) {
            delta = Math.min(delta, edge.remaining);
        }
        for (Edge edge = previous[end]; edge != null; edge = previous[edge.from]) {
            edge.remaining -= delta;
            edge.reverse.remaining += delta;
        }
        return true;
    }

    private static final class Edge {
        private final int from;
        private final int to;
        private int remaining;
        private Edge reverse;

        private Edge(int from, int to, int remaining) {
            this.from = from;
            this.to = to;
            this.remaining = remaining;
        }
    }
}

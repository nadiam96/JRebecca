/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.graph;

import ir.ac.ut.rebbeca.annotations.StateVar;
import ir.ac.ut.rebbeca.core.domain.ReactiveClass;
import ir.ac.ut.rebbeca.core.domain.ReflectionUtils;
import ir.ac.ut.rebbeca.core.domain.State;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mohammad TRB
 */
public class StateGraphFactory {

    public StateGraphFactory() {
    }

    public VisGraph generateStateGraph(State state) {

        List<VisNode> nodes = new ArrayList<>();
        List<VisEdge> edges = new ArrayList<>();

        Map<String, Point> position = new HashMap<>();
        position.put(state.getId(), new Point(0, 0));
        determinePositions(state, position);

        generateGraph(state, nodes, edges);
        finalizeData(edges, nodes, position);
        return new VisGraph(edges, nodes);
    }

    private void determinePositions(State state, Map<String, Point> positions) {

        List<State> children = state.getNext();

        for (int i = 0; i < children.size(); i++) {
            State child = children.get(i);
            if (positions.keySet().contains(child.getId())) {
                return;
            }
            positions.put(child.getId(), new Point((i - children.size() / 2) * 250, positions.get(state.getId()).y + 150));
            determinePositions(child, positions);
        }

    }

    private void finalizeData(List<VisEdge> edges, List<VisNode> nodes, Map<String, Point> positions) {
        Map<String, Integer> idMap = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            idMap.put(nodes.get(i).getIdStr(), i + 1);
            nodes.get(i).setId(i + 1);

        }

        repairDuplicatePositions(positions);
        centerifyPositions(positions);
        for (VisNode node : nodes) {

            node.x = positions.get(node.getIdStr()) != null ? positions.get(node.getIdStr()).x : 500;
            node.y = positions.get(node.getIdStr()) != null ? positions.get(node.getIdStr()).y : 500;
        }

        for (VisEdge edge : edges) {
            edge.setFrom(idMap.get(edge.getFromStr()));
            edge.setTo(idMap.get(edge.getToStr()));
        }
    }

    private void centerifyPositions(Map<String, Point> positions) {
        Map<Integer, Set<Point>> levels = new HashMap<>();
        Map<Integer, Integer> shiftLevels = new HashMap<>();

        for (Point p : positions.values()) {
            if (levels.get(p.y) == null) {
                Set<Point> points = new HashSet<>();
                points.add(p);
                levels.put(p.y, points);
            } else {
                levels.get(p.y).add(p);
            }
        }

        int min = Collections.min(levels.keySet());
        if (min < 0) {
            Map<Integer, Set<Point>> normalized = new HashMap<>();
            for (int i : levels.keySet()) {
                normalized.put(i - min, levels.get(i));
            }
            levels = new HashMap<>(normalized);
        }

        for (int i = 0; levels.get(i) != null; i += 150) {
            List<Integer> xArr = new ArrayList<>();
            for (Point p : levels.get(i)) {
                xArr.add(p.x);
            }
            Double x = xArr.stream().mapToInt(val -> val).average().orElse(-1);
            shiftLevels.put(i, x.intValue());
        }

        for (int i : levels.keySet()) {
            int shift = shiftLevels.get(i);
            for (Point p : levels.get(i)) {
                p.x -= shift;
            }
        }
    }

    private int suggestPositionHorizontal(int y, Set<Point> points) {
        if (points == null || points.isEmpty()) {
            return 0;
        }
        List<Integer> occupied = new ArrayList<>();
        for (Point point : points) {
            if (point.y == y) {
                occupied.add(point.x);
            }
        }

        return occupied.isEmpty() ? 0 : Collections.max(occupied) + 250;
    }

    private void repairDuplicatePositions(Map<String, Point> positions) {
        List<String> ids = new ArrayList<>(positions.keySet());
        if (ids.size() < 2) {
            return;
        }

        repairDuplicateRecursion(0, ids, positions);
    }

    private void repairDuplicateRecursion(int index, List<String> ids, Map<String, Point> positions) {
        if (index == ids.size()) {
            return;
        }
        for (String id : ids) {
            Point point = positions.get(id);
            Point sample = positions.get(ids.get(index));
            if (point == null || sample == null) {
                continue;
            }
            if (sample.x == point.x && sample.y == point.y) {
                point.x = suggestPositionHorizontal(sample.y, new HashSet<>(positions.values()));

            }
        }

        repairDuplicateRecursion(index + 1, ids, positions);

    }

    private void generateGraph(State state, List<VisNode> nodes, List<VisEdge> edges) {
        VisNode currentNode = makeGraphNode(state);

        for (VisNode node : nodes) {
            if (node.getIdStr().equals(state.getId())) {
                return;
            }
        }
        nodes.add(currentNode);
        List<State> surroundings = state.getNext();
        for (State child : surroundings) {
            VisEdge newEdge = new VisEdge(state.getId(), child.getId(), "");
            if (!isEdgePresent(newEdge, edges)) {
                edges.add(newEdge);
                generateGraph(child, nodes, edges);
            }
        }
        return;
    }

    private Boolean isEdgePresent(VisEdge edge, List<VisEdge> edges) {
        for (VisEdge e : edges) {
            if (e.getFromStr().equals(edge.getFromStr()) && e.getToStr().equals(edge.getToStr())) {
                return true;
            } else if (e.getToStr().equals(edge.getFromStr()) && e.getFromStr().equals(edge.getToStr())) {
                return true;
            }
        }
        return false;
    }

    private VisNode makeGraphNode(State state) {
        return new VisNode(state.getId(), getLabel(state));
    }

    private String getLabel(State state) {
        StringBuilder builder = new StringBuilder();
        builder.append("State: ");
        builder.append(state.getId());
        builder.append("\n");
        for (ReactiveClass actor : state.getReactiveClasss()) {
            builder.append("Actor: ");
            builder.append(actor.getId());
            builder.append("\n");
            for (Field field : ReflectionUtils.findFieldsByAnnotation(actor.getClass(), StateVar.class)) {
                try {
                    builder.append(field.getName() + ": ");
                    builder.append(field.get(actor));
                    builder.append("\n");
                } catch (IllegalArgumentException ex) {
                } catch (IllegalAccessException ex) {
                }
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public class Point {

        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }
}

/*
 * Copyright 2019 mohammad.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.ac.ut.rebbeca.core.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author mohammad
 */
public class State implements Serializable {

    public List<ReactiveClass> actors;
    public List<State> next;
    public int maxDepth;

    public State() {

    }

    public void generateNext(int depth) {

        depth++;
        if (depth > maxDepth) {
            return;
        }
        next = new ArrayList<>();
        List<ReactiveClass> copyReactiveClass = new ArrayList<>(actors).stream().map(act -> act.copy(act.getClass())).collect(Collectors.toList());
        ArrayList<State> copyNext = new ArrayList<>(next);
        for (ReactiveClass actor : copyReactiveClass) {
            try {
                next.add(actor.handleMethod(new State(new ArrayList<>(copyReactiveClass), new ArrayList<>(copyNext))));
            } catch (NoMessageException ex) {
            }
        }
        for (State nxt : next) {
            nxt.generateNext(depth);
        }
    }

    public void generateNextStep() {
        next = new ArrayList<>();
        List<ReactiveClass> copyReactiveClass = new ArrayList<>(actors).stream().map(act -> act.copy(act.getClass())).collect(Collectors.toList());
        List<State> copyNext = new ArrayList<>(next);
        for (ReactiveClass actor : copyReactiveClass) {
            try {
                State successor = actor.handleMethod(new State(new ArrayList<>(copyReactiveClass), new ArrayList<>(copyNext)));
                successor.setNext(new ArrayList<>());
                next.add(successor);
            } catch (NoMessageException ex) {
            }
        }
        for (State st : next) {
            st.setNext(new ArrayList<>());
        }
    }

    public List<ReactiveClass> getReactiveClasss() {
        return actors;
    }

    public void setReactiveClasss(List<ReactiveClass> actors) {
        this.actors = actors;
    }

    public List<State> getNext() {
        if (next == null) {
            next = new ArrayList<>();
        }
        return next;
    }

    public void setNext(List<State> next) {
        this.next = next;
    }

    public State(List<ReactiveClass> actors, List<State> next) {
        this.actors = actors;
        this.next = next;
    }

    public State(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public ReactiveClass loadReactiveClassById(Long id) {
        for (ReactiveClass act : actors) {
            if (act.getId().equals(id)) {
                return act;
            }
        }
        return null;
    }

    public String getId() {
        return String.valueOf(this.hashCode());
    }

    public List<State> getLeaves() {
        List<State> leaves = new ArrayList<>();
        getLeavesRecursive(this, leaves);
        return leaves;
    }

    public static void getLeavesRecursive(State state, List<State> result) {
        if (result == null) {
            result = new ArrayList<>();
        }
        if (state.getNext() == null || state.getNext().isEmpty()) {
            result.add(state);
        } else {
            for (State child : state.getNext()) {
                getLeavesRecursive(child, result);
            }
        }
    }

    public ReactiveClass loadRebbecById(Long id) {
        for (ReactiveClass x : actors) {
            if (x.getId().equals(id)) {
                return x;
            }
        }
        return null;
    }

}

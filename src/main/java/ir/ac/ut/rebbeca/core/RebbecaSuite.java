/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.rebbeca.core;

import ir.ac.ut.rebbeca.core.domain.ReactiveClass;
import ir.ac.ut.rebbeca.core.domain.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.MainDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.MainRebecDefinition;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.OrdinaryPrimitiveType;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.RebecaModel;

/**
 *
 * @author mohammad
 */
public abstract class RebbecaSuite {
    
    protected State state;

    protected abstract List<ReactiveClass> setupActors();

    private State generateState(int maxDepth) {
        State state = new State(maxDepth);
        List<ReactiveClass> actors = setupActors();
        state.setReactiveClasss(actors);
        return state;
    }

    protected abstract void initialCalls(List<ReactiveClass> actors);
    
    public State getInitialState(int maxDepth){
        state = generateState(maxDepth);
        initialCalls(state.getReactiveClasss());
        return state;
    }
}

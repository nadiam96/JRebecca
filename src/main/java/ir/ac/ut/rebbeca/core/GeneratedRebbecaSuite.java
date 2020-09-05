/*
 * Copyright 2020 mohammad.
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
package ir.ac.ut.rebbeca.core;

import ir.ac.ut.rebbeca.core.domain.ReactiveClass;
import ir.ac.ut.rebbeca.core.domain.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.joor.Reflect;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.BaseClassDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.MainDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.MainRebecDefinition;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.OrdinaryPrimitiveType;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.ReactiveClassDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.RebecaModel;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.TermPrimary;

/**
 *
 * @author mohammad
 */
public abstract class GeneratedRebbecaSuite extends RebbecaSuite {

    @Override
    protected List<ReactiveClass> setupActors() {

        List<ReactiveClass> result = new ArrayList<>();
        RebecaModel model = getRebecaModel();

        List<Reflect> compiledSources = getCompiledSources();
        List<String> actorDeclarations = extractActorDeclarations(model);
        Map<String, Long> idTable = assignIdToActors(actorDeclarations);
        Map<String, String> varToType = extractVarToTypeMap(model);
        Map<String, List<String>> bindings = extractBindings(model);

        for (String actor : actorDeclarations) {
            String typeName = varToType.get(actor);
            String fullName = "ir.ac.ut.generatedSources." + typeName;
            for (Reflect ref : compiledSources) {
                if (ref.create().type().getName().equals(fullName)) {
                    Long id = idTable.get(actor);
                    Map<String, Long> localIdTable = new HashMap<>();
                    List<String> bindin = bindings.get(actor);
                    List<String> innerBinding = getOutToInName(model, typeName);
                    for (int i = 0; i < bindin.size(); i++) {
                        localIdTable.put(innerBinding.get(i), idTable.get(bindin.get(i)));
                    }
                    try {
                        Class clazz = Class.forName(fullName);
                        Constructor ctor = clazz.getConstructor();
                        ReactiveClass actorInstance = (ReactiveClass) ctor.newInstance();
                        actorInstance.setId(id);
                        actorInstance.setKnownRebecs(localIdTable);
                        actorInstance.setQueue(new ArrayList<>());
                        actorInstance.setMaxQueueSize(getQueueSize(typeName, model));
                        for (String key : localIdTable.keySet()) {
                            try {
                                Field field = actorInstance.getClass().getField(key);
                                field.set(actorInstance, new ReactiveClass(localIdTable.get(key)));
                            } catch (NoSuchFieldException ex) {
                            } catch (SecurityException ex) {
                            } catch (IllegalArgumentException ex) {
                            } catch (IllegalAccessException ex) {
                            }
                        }

                        result.add((ReactiveClass) actorInstance);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected void initialCalls(List<ReactiveClass> actors) {
        for(ReactiveClass actor : actors){
            Class clazz = actor.getClass();
            try {
                Method meth = ReflectionUtils.findMethodByName("virtual_constructor", clazz);
                meth.invoke(actor);
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }

    private MainDeclaration extractMainDeclaration(RebecaModel rebecaModel) {
        return rebecaModel.getRebecaCode().getMainDeclaration();
    }

    public abstract RebecaModel getRebecaModel();

    public abstract List<Reflect> getCompiledSources();

    private int getQueueSize(String typeName, RebecaModel model) {
        for (ReactiveClassDeclaration dec : model.getRebecaCode().getReactiveClassDeclaration()) {
            BaseClassDeclaration decl = (BaseClassDeclaration) dec;
            if (dec.getName().equals(typeName)) {
                return dec.getQueueSize();
            }

        }
        return 10;
    }

    private List<String> getOutToInName(RebecaModel model, String actorType) {
        List<String> result = new ArrayList<>();
        for (ReactiveClassDeclaration dec : model.getRebecaCode().getReactiveClassDeclaration()) {
            BaseClassDeclaration decl = (BaseClassDeclaration) dec;
            if (dec.getName().equals(actorType)) {
                result = dec.getKnownRebecs().stream().map(x -> {
                    return x.getVariableDeclarators().iterator().next().getVariableName();
                }).collect(Collectors.toList());
                return result;
            }

        }
        return result;
    }

    private List<String> extractActorDeclarations(RebecaModel rebecaModel) {
        List<String> result = new ArrayList<>();
        MainDeclaration main = extractMainDeclaration(rebecaModel);
        if (main == null) {
            return result;
        }
        main.getMainRebecDefinition().stream().forEach(x -> result.add(x.getName()));
        return result;
    }

    private Map<String, Long> assignIdToActors(List<String> actors) {
        Long id = Long.valueOf("1");
        Map<String, Long> result = new HashMap<>();
        for (String actor : actors) {
            result.put(actor, id);
            id++;
        }
        return result;
    }

    private Map<String, String> extractVarToTypeMap(RebecaModel rebecaModel) {
        Map<String, String> result = new HashMap<>();
        MainDeclaration main = extractMainDeclaration(rebecaModel);
        if (main != null) {
            for (MainRebecDefinition dec : main.getMainRebecDefinition()) {
                OrdinaryPrimitiveType type = (OrdinaryPrimitiveType) dec.getType();
                result.put(dec.getName(), type.getName());
            }
        }
        return result;
    }

    private Map<String, List<String>> extractBindings(RebecaModel rebecaModel) {
        Map<String, List<String>> result = new HashMap<>();
        MainDeclaration main = extractMainDeclaration(rebecaModel);
        if (main != null) {
            for (MainRebecDefinition dec : main.getMainRebecDefinition()) {
                result.put(dec.getName(), dec.getBindings().stream().map((x) -> {
                    TermPrimary y = (TermPrimary) x;
                    return y.getName();
                }
                ).collect(Collectors.toList()));
            }
        }
        return result;
    }

}

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
package ir.ac.ut.sourceGenerator;

import edu.emory.mathcs.backport.java.util.Arrays;
import ir.ac.ut.rebbeca.annotations.KnownRebbec;
import ir.ac.ut.rebbeca.annotations.MsgSrv;
import ir.ac.ut.rebbeca.annotations.StateVar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.ConstructorDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.FieldDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.FormalParameterDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.MsgsrvDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.OrdinaryPrimitiveType;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.ReactiveClassDeclaration;

/**
 *
 * @author mohammad
 */
public abstract class RebecaClassCodeGenerator extends ClassCodeGenerator {

    @Override
    protected String getPackage() {
        return "ir.ac.ut.generatedSources";
    }

    @Override
    protected String getClassName() {
        return getReactiveClassDeclaration().getName();
    }

    @Override
    protected List<String> getImports() {
        return java.util.Arrays.asList("ir.ac.ut.rebbeca.core.domain.State", "ir.ac.ut.rebbeca.core.domain.ReactiveClass",
                "ir.ac.ut.rebbeca.core.domain.Message", "ir.ac.ut.rebbeca.annotations.KnownRebbec",
                "ir.ac.ut.rebbeca.annotations.MsgSrv", "ir.ac.ut.rebbeca.annotations.StateVar");
    }

    @Override
    protected List<CustomField> getFields() {
        List<CustomField> result = new ArrayList<>();
        for (FieldDeclaration stateVar : getReactiveClassDeclaration().getStatevars()) {
            OrdinaryPrimitiveType type = (OrdinaryPrimitiveType) stateVar.getType();
            List<Class> annotations = new ArrayList<>();
            annotations.add(StateVar.class);
            CustomField field = new CustomField(stateVar.getVariableDeclarators().iterator().next().getVariableName(), type.getName(), "public", annotations);
            result.add(field);
        }

        for (FieldDeclaration knownRebec : getReactiveClassDeclaration().getKnownRebecs()) {
            List<Class> annotations = new ArrayList<>();
            annotations.add(KnownRebbec.class);
            CustomField field = new CustomField(knownRebec.getVariableDeclarators().iterator().next().getVariableName(), "ReactiveClass", "public", annotations);
            result.add(field);
        }
        return result;
    }

    @Override
    protected List<CustomMethod> getMethods() {
        List<CustomMethod> result = new ArrayList<>();
        for (MsgsrvDeclaration meth : getReactiveClassDeclaration().getMsgsrvs()) {

            List<Class> annotations = new ArrayList<>();
            annotations.add(MsgSrv.class);

            List<CustomParameter> params = new ArrayList<>();
            params.add(new CustomParameter("State", "inputState"));
            params.add(new CustomParameter("ReactiveClass", "sender"));
            for (FormalParameterDeclaration par : meth.getFormalParameters()) {
                OrdinaryPrimitiveType t = (OrdinaryPrimitiveType) par.getType();
                params.add(new CustomParameter(t.getName(), par.getName()));
            }
            CustomMethod newMethod = new CustomMethod(meth.getName(), getInnerBody(meth.getLineNumber(), meth.getEndLineNumber()), "State", "public", annotations, params) {
                @Override
                protected String modifyBody(String innerBody) {
                    return addReturnStatement(javayifyRebecaCode(innerBody, getKnownRebecs(), getAllMethodNames()));
                }
            };
            result.add(newMethod);
        }
        addVirtualConstructors(result);
        return result;
    }

    @Override
    protected List<CustomConstructor> getConstructors() {
        List<CustomConstructor> result = new ArrayList<>();
        for (ConstructorDeclaration ctor : getReactiveClassDeclaration().getConstructors()) {
            if(ctor.getLineNumber() == null){
                continue;
            }
            List<CustomParameter> params = new ArrayList<>();
            for (FormalParameterDeclaration par : ctor.getFormalParameters()) {
                OrdinaryPrimitiveType t = (OrdinaryPrimitiveType) par.getType();
                params.add(new CustomParameter(t.getName(), par.getName()));
            }

            CustomConstructor newCtor = new CustomConstructor(getReactiveClassDeclaration().getName(), params, getInnerBody(ctor.getLineNumber(), ctor.getEndLineNumber()), "public", new ArrayList<Class>()) {
                @Override
                protected String modifyBody(String innerBody) {
                    return javayifyRebecaCode(innerBody, getKnownRebecs(), getAllMethodNames());
                }
            };
            result.add(newCtor);
        }
        return result;
    }

    @Override
    protected String getSuperClass() {
        return "ReactiveClass";
    }

    protected abstract ReactiveClassDeclaration getReactiveClassDeclaration();

    protected abstract String getRebecaSource();

    protected abstract Set<String> getAllMethodNames();

    private void addVirtualConstructors(List<CustomMethod> methods) {
        for (CustomConstructor ctor : getConstructors()) {
            List<Class> annotations = new ArrayList<>();
            annotations.add(MsgSrv.class);
            methods.add(new CustomMethod("virtual_constructor", ctor.getInnerBody(), "void", "public", annotations, ctor.getParams()) {
                @Override
                protected String modifyBody(String innerBody) {
                    return javayifyRebecaCode(innerBody, getKnownRebecs(), getAllMethodNames());
                }
            });
        }
    }

    private Set<String> getKnownRebecs() {
        Set<String> result = new HashSet<>();
        getReactiveClassDeclaration().getKnownRebecs().stream().forEach(x -> {
            result.add(x.getVariableDeclarators().iterator().next().getVariableName());
        });
        return result;
    }

    private String javayifyRebecaCode(String rebCode, Set<String> knownRebecs, Set<String> methodNames) {
        String result = rebCode.replaceAll("self.", "this.");
        result = handleEquals(result, knownRebecs);
        result = handleMessageCalls(result, knownRebecs, methodNames);

        return repairBraces(result);
    }

    private String repairBraces(String body) {
        int closing = body.length() - (body.replaceAll("\\}", "").length());
        int opening = body.length() - (body.replaceAll("\\{", "").length());
        if (opening > closing) {
            StringBuilder builder = new StringBuilder();
            builder.append(body);
            builder.append("\n");
            builder.append("}");
            return builder.toString();
        } else if(opening < closing){
            return body.substring(0, body.lastIndexOf("}") - 1);
        }else {
            return body;
        }
    }

    private String handleEquals(String methodBody, Set<String> knownRebecs) {
        String[] splitted = methodBody.split("==");
        String result = "";
        if (splitted.length == 1) {
            return methodBody;
        } else {
            for (int i = 0; i < splitted.length; i++) {
                if (i == 0) {
                    result += splitted[i];
                    continue;
                }

                String splittedCurrent = splitted[i];
                if (splittedCurrent.length() < 2) {
                    continue;
                }
                Boolean found = false;
                for (int j = 1; j < splittedCurrent.length(); j++) {
                    String temp = splittedCurrent.substring(0, j);
                    temp = temp.replaceAll(" ", "");
                    if (knownRebecs.contains(temp)) {
                        found = true;
                        result += ".equals(";
                        result += temp;
                        result += ")";
                        result += splittedCurrent.substring(j, splittedCurrent.length() - 1);
                        break;
                    }
                }
                if (!found) {
                    result += "==";
                    result += splittedCurrent;
                }
            }
        }
        return result;
    }

    private String handleMessageCalls(String methodBody, Set<String> knownRebecs, Set<String> methodNames) {
        String result = methodBody;
        knownRebecs.add("this");
        int maxMethSize = methodNames.stream().map(x -> x.length()).sorted((x, y) -> y.compareTo(x)).collect(Collectors.toList()).get(0) + 1;
        for (String known : knownRebecs) {
            String splitter = known + "\\.";
            String[] splitted = result.split(splitter);
            if (splitted.length == 1) {
                continue;
            }
            String temp = "";
            for (int i = 0; i < splitted.length; i++) {

                String part = splitted[i];
                if (i == 0) {
                    temp += part;
                    continue;
                }
                if (part.replaceAll(" ", "").startsWith("messagePushBack") || part.replaceAll(" ", "").startsWith("equals")) {
                    temp += known + ".";
                    temp += part;
                    continue;
                }
                if (part.length() < 2) {
                    temp += part;
                    continue;
                }
                Boolean found = false;
                for (int j = 1; j < Math.min(maxMethSize, part.length()); j++) {
                    String portion = part.substring(0, j);
                    portion = portion.replaceAll(" ", "");
                    if (methodNames.contains(portion)) {
                        found = true;
                        if (!known.equals("this")) {
                            temp += "inputState.loadRebbecById(knownRebecs.get(\"" + known + "\"))" + ".messagePushBack(new Message(\"" + portion + "\",this";
                        } else {
                            temp += (known + ".messagePushBack(new Message(\"" + portion + "\",this");
                        }
                        String x = part.substring(part.indexOf("("), part.length() - 1);
                        x = x.substring(1, x.indexOf(")"));
                        if (!x.isEmpty()) {
                            for (String param : x.split(",")) {
                                temp += "," + param;
                            }
                        }
                        temp += "))";
                        String tail = part.substring(part.indexOf(";"), part.length() - 1);
                        temp += tail;
                        if (tail.isEmpty() && part.endsWith((";"))) {
                            temp += ";";
                        }
                        break;
                    }

                }
                if (!found) {
                    temp += part;
                }
            }
            result = temp;
        }

        return result;
    }

    private String addReturnStatement(String methodBody) {
        return methodBody + "\n" + "return inputState;";
    }

    private String getInnerBody(int startLine, int endLine) {
        BufferedReader br = new BufferedReader(new StringReader(getRebecaSource()));
        StringBuilder builder = new StringBuilder();
        String line;
        int counter = 0;
        try {
            while ((line = br.readLine()) != null) {
                if (startLine -1 <= counter && counter <= endLine) {
                    builder.append(line);
                    builder.append("\n");
                }
                counter++;
            }
        } catch (IOException ex) {
        }
        String result = builder.toString();
        return result.substring(result.indexOf("{") + 1, result.lastIndexOf("}") - 1);
    }
    
    
}

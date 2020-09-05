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

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 * @author mohammad
 */
public abstract class CustomConstructor extends CodeElement {

    private String type;
    private List<CustomParameter> params;
    private String innerBody;

    public CustomConstructor(String type, List<CustomParameter> params, String innerBody, String accessModifier, List<Class> annotations) {
        super(accessModifier, annotations);
        this.type = type;
        this.params = params;
        this.innerBody = innerBody;
    }

    @Override
    public String getGeneratedCode() {
        StringBuilder builder = new StringBuilder();
        for (Class ann : annotations) {
            builder.append("@" + ann.getSimpleName());
            builder.append("\n");
        }
        builder.append(accessModifier);
        builder.append(" ");
        builder.append(type);
        builder.append(" ");
        builder.append("(");
        for (int i = 0; i < params.size(); i++) {
            CustomParameter parameter = params.get(i);
            builder.append(parameter.type);
            builder.append(" ");
            builder.append(parameter.name);
            if (i != params.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append(")");
        builder.append("\n");
        builder.append("{");
        builder.append("\n");
        builder.append(modifyBody(innerBody));
        builder.append("\n");
        builder.append("}");
        return builder.toString();
    }

    public String getInnerBody(){
        return innerBody;
    }

    public List<CustomParameter> getParams() {
        return params;
    }
    
    protected abstract String modifyBody(String innerBody);
}

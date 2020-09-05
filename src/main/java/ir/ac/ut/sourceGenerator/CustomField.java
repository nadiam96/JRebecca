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
public class CustomField extends CodeElement {

    private String name;
    private String type;

    public CustomField(String name, String type, String accessModifier, List<Class> annotations) {
        super(accessModifier, annotations);
        this.name = name;
        this.type = type;
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
        builder.append(name);
        builder.append(";");
        return builder.toString();
    }

}

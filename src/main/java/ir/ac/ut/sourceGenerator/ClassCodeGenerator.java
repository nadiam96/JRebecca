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

import java.util.List;

/**
 *
 * @author mohammad
 */
public abstract class ClassCodeGenerator {

    protected abstract String getPackage();

    protected abstract String getClassName();

    protected abstract List<String> getImports();

    protected abstract List<CustomField> getFields();

    protected abstract List<CustomMethod> getMethods();

    protected abstract List<CustomConstructor> getConstructors();

    protected abstract String getSuperClass();

    public String generateSource() {
        StringBuilder builder = new StringBuilder();

        builder.append("package ");
        builder.append(getPackage());
        builder.append(";");
        builder.append("\n");

        for (String imp : getImports()) {
            builder.append("import ");
            builder.append(imp);
            builder.append(";");
            builder.append("\n");
        }
        builder.append("\n");

        builder.append("public class ");
        builder.append(getClassName());
        builder.append(" ");

        String superClass = getSuperClass();
        if (superClass != null && !superClass.isEmpty()) {
            builder.append("extends ");
            builder.append(superClass);
        }

        builder.append("{");
        builder.append("\n");
        for (CustomField field : getFields()) {
            builder.append(field.getGeneratedCode());
            builder.append("\n");
        }
        for (CustomConstructor ctor : getConstructors()) {
            builder.append(repairBraces(ctor.getGeneratedCode()));
            builder.append("\n");
        }
        for (CustomMethod meth : getMethods()) {
            builder.append(repairBraces(meth.getGeneratedCode()));
            builder.append("\n");
        }
        builder.append("\n");
        builder.append("}");

        return builder.toString();
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
        } else {
            return body;
        }
    }
}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joor.Reflect;
import org.rebecalang.compiler.modelcompiler.RebecaCompiler;
import org.rebecalang.compiler.modelcompiler.SymbolTable;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.MsgsrvDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.ReactiveClassDeclaration;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.RebecaModel;
import org.rebecalang.compiler.utils.CompilerFeature;
import static org.rebecalang.compiler.utils.CompilerFeature.CORE_2_3;
import org.rebecalang.compiler.utils.Pair;

/**
 *
 * @author mohammad
 */
public class RebecaCodeFactory {

    private File rebecaFile;
    private SymbolTable symbolTable;
    private RebecaModel rebecaModel;
    Map<String, String> generatedCodes;

    public RebecaCodeFactory(File rebecaFile) {
        this.rebecaFile = rebecaFile;
        RebecaCompiler rebecaCompiler = new RebecaCompiler();
        CompilerFeature compilerFeature = (CompilerFeature) CORE_2_3;
        Set<CompilerFeature> cfSet = new HashSet<>();
        cfSet.add(compilerFeature);
        Pair<RebecaModel, SymbolTable> generatedPair = rebecaCompiler.compileRebecaFile(rebecaFile, cfSet, true);
        this.symbolTable = generatedPair.getSecond();
        this.rebecaModel = generatedPair.getFirst();
    }

    public List<Reflect> getCompiledClasses() {
        List<Reflect> compiledClasses = new ArrayList<>();
        generatedCodes = generateClassCodes();
        for (String cls : generatedCodes.keySet()) {
            compiledClasses.add(Reflect.compile("ir.ac.ut.generatedSources." + cls, generatedCodes.get(cls)).create());
        }
        return compiledClasses;
    }

    public Map<String, String> generateClassCodes() {

        if (generatedCodes == null) {
            generatedCodes = new HashMap<>();
            for (ReactiveClassDeclaration dec : rebecaModel.getRebecaCode().getReactiveClassDeclaration()) {
                generatedCodes.put(dec.getName(),
                        new RebecaClassCodeGenerator() {
                            @Override
                            protected ReactiveClassDeclaration getReactiveClassDeclaration() {
                                return dec;
                            }

                            @Override
                            protected String getRebecaSource() {
                                return readFile(rebecaFile);
                            }

                            @Override
                            protected Set<String> getAllMethodNames() {
                                return getAllMethNames();
                            }
                        }.generateSource()
                );

            }
        }
        return generatedCodes;
    }

    private Set<String> getAllMethNames() {
        Set<String> result = new HashSet<>();
        for (ReactiveClassDeclaration dec : rebecaModel.getRebecaCode().getReactiveClassDeclaration()) {
            for (MsgsrvDeclaration msg : dec.getMsgsrvs()) {
                result.add(msg.getName());
            }
        }
        return result;
    }

    private String readFile(File rf) {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(rf));
            try {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    builder.append(line);
                    builder.append("\n");
                }
            } catch (IOException ee) {
            }
        } catch (FileNotFoundException e) {
        }
        return builder.toString();
    }

    public File getRebecaFile() {
        return rebecaFile;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public RebecaModel getRebecaModel() {
        return rebecaModel;
    }
}

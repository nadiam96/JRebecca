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

import ir.ac.ut.rebbeca.core.domain.State;
import ir.ac.ut.sourceGenerator.RebecaCodeFactory;
import ir.ac.ut.uploadManager.ZipFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joor.Reflect;
import org.rebecalang.compiler.modelcompiler.corerebeca.objectmodel.RebecaModel;

/**
 *
 * @author mohammad
 */
public abstract class StateFactory {

    private RebecaCodeFactory rebecaCodeFactory;
    private GeneratedRebbecaSuite suite;

    public State generateState(File file) {

        rebecaCodeFactory = new RebecaCodeFactory(file);
        suite = new GeneratedRebbecaSuite() {
            @Override
            public RebecaModel getRebecaModel() {
                return rebecaCodeFactory.getRebecaModel();
            }

            @Override
            public List<Reflect> getCompiledSources() {
                return rebecaCodeFactory.getCompiledClasses();
            }
        };
        return suite.getInitialState(getDepth());
    }

    protected abstract int getDepth();

    public File getZippedSources() {

        return ZipFile.zipifyFiles(rebecaCodeFactory.generateClassCodes());

    }

}

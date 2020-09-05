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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mohammad
 */
public class Message implements Serializable{
    
    public String methodName;
    public Object[] args;

    public Message(String methodName, Object... args) {
        this.methodName = methodName;
        this.args = args;
    }

    public Message() {
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    
}
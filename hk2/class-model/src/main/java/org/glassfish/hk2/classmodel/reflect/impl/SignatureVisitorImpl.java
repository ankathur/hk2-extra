/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.hk2.classmodel.reflect.impl;

import org.glassfish.hk2.classmodel.reflect.InterfaceModel;
import org.glassfish.hk2.external.org.objectweb.asm.Opcodes;
import org.glassfish.hk2.external.org.objectweb.asm.signature.SignatureVisitor;

import java.util.*;

/**
 * Signature visitor to visit parameterized declarations
 *
 * @author Jerome Dochez
 */
public class SignatureVisitorImpl extends SignatureVisitor {

    private final TypeBuilder typeBuilder;
    private final Stack<ParameterizedInterfaceModelImpl> stack = new Stack<ParameterizedInterfaceModelImpl>();
    private final Map<String, ParameterizedInterfaceModelImpl> formalTypes = new HashMap<String, ParameterizedInterfaceModelImpl>();
    private final Stack<String> formalTypesNames = new Stack<String>();
    private final List<ParameterizedInterfaceModelImpl> parameterizedIntf = new ArrayList<ParameterizedInterfaceModelImpl>();

    public SignatureVisitorImpl(TypeBuilder typeBuilder) {
        super(Opcodes.ASM5);
        
        this.typeBuilder = typeBuilder;
    }

    Collection<ParameterizedInterfaceModelImpl> getImplementedInterfaces() {
        return Collections.unmodifiableCollection(parameterizedIntf);
    }


    @Override
    public void visitFormalTypeParameter(String s) {
        formalTypesNames.push(s);
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return this;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SignatureVisitor visitInterface() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitBaseType(char c) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitTypeVariable(String s) {
        if (formalTypes.containsKey(s)) {
            String interfaceName = formalTypes.get(s).getName();
            TypeProxy<InterfaceModel> interfaceTypeProxy = typeBuilder.getHolder(
                    interfaceName, InterfaceModel.class);
            if (interfaceTypeProxy!=null) {
                ParameterizedInterfaceModelImpl childParameterized = new ParameterizedInterfaceModelImpl(interfaceTypeProxy);
                if (!stack.empty()) {
                    stack.peek().addParameterizedType(childParameterized);
                }
            }
        }
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitClassType(String s) {
        String interfaceName = org.glassfish.hk2.external.org.objectweb.asm.Type.getObjectType(s).getClassName();
        TypeProxy<InterfaceModel> interfaceTypeProxy = typeBuilder.getHolder(interfaceName, InterfaceModel.class);
        if (interfaceTypeProxy!=null) {
            ParameterizedInterfaceModelImpl childParameterized = new ParameterizedInterfaceModelImpl(interfaceTypeProxy);
            if (!s.equals("java/lang/Object")) {
                if (formalTypesNames.empty()) {
                    if (!stack.empty()) {
                        stack.peek().addParameterizedType(childParameterized);
                    }
                } else {
                    formalTypes.put(formalTypesNames.pop(), childParameterized);
                }
            }
            stack.push(childParameterized);
        }
    }

    @Override
    public void visitInnerClassType(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitTypeArgument() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SignatureVisitor visitTypeArgument(char c) {
        return this;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void visitEnd() {
        if (stack.empty()) return;
        ParameterizedInterfaceModelImpl lastElement = stack.pop();
        if (stack.isEmpty()) {
            parameterizedIntf.add(lastElement);
        }
    }
}

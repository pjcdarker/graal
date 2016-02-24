/*
 * Copyright (c) 2009, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.lir.gen;

import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.LIRKind;
import jdk.vm.ci.meta.PlatformKind;
import jdk.vm.ci.meta.Value;

import com.oracle.graal.lir.Variable;

/**
 * This class traverses the HIR instructions and generates LIR instructions from them.
 */
public abstract class ArithmeticLIRGenerator implements ArithmeticLIRGeneratorTool {

    LIRGenerator lirGen;

    public LIRGenerator getLIRGen() {
        return lirGen;
    }

    // automatic derived reference handling

    protected abstract boolean isNumericInteger(PlatformKind kind);

    protected abstract Variable emitAdd(LIRKind resultKind, Value a, Value b, boolean setFlags);

    @Override
    public final Variable emitAdd(Value aVal, Value bVal, boolean setFlags) {
        LIRKind resultKind;
        Value a = aVal;
        Value b = bVal;

        if (isNumericInteger(a.getPlatformKind())) {
            LIRKind aKind = a.getLIRKind();
            LIRKind bKind = b.getLIRKind();
            assert a.getPlatformKind() == b.getPlatformKind();

            if (aKind.isUnknownReference()) {
                resultKind = aKind;
            } else if (bKind.isUnknownReference()) {
                resultKind = bKind;
            } else if (aKind.isValue() && bKind.isValue()) {
                resultKind = aKind;
            } else if (aKind.isValue()) {
                if (bKind.isDerivedReference()) {
                    resultKind = bKind;
                } else {
                    AllocatableValue allocatable = getLIRGen().asAllocatable(b);
                    resultKind = bKind.makeDerivedReference(allocatable);
                    b = allocatable;
                }
            } else if (bKind.isValue()) {
                if (aKind.isDerivedReference()) {
                    resultKind = aKind;
                } else {
                    AllocatableValue allocatable = getLIRGen().asAllocatable(a);
                    resultKind = aKind.makeDerivedReference(allocatable);
                    a = allocatable;
                }
            } else {
                resultKind = aKind.makeUnknownReference();
            }
        } else {
            resultKind = LIRKind.combine(a, b);
        }

        return emitAdd(resultKind, a, b, setFlags);
    }

    protected abstract Variable emitSub(LIRKind resultKind, Value a, Value b, boolean setFlags);

    @Override
    public final Variable emitSub(Value aVal, Value bVal, boolean setFlags) {
        LIRKind resultKind;
        Value a = aVal;
        Value b = bVal;

        if (isNumericInteger(a.getPlatformKind())) {
            LIRKind aKind = a.getLIRKind();
            LIRKind bKind = b.getLIRKind();
            assert a.getPlatformKind() == b.getPlatformKind();

            if (aKind.isUnknownReference()) {
                resultKind = aKind;
            } else if (bKind.isUnknownReference()) {
                resultKind = bKind;
            }

            if (aKind.isValue() && bKind.isValue()) {
                resultKind = aKind;
            } else if (bKind.isValue()) {
                if (aKind.isDerivedReference()) {
                    resultKind = aKind;
                } else {
                    AllocatableValue allocatable = getLIRGen().asAllocatable(a);
                    resultKind = aKind.makeDerivedReference(allocatable);
                    a = allocatable;
                }
            } else if (aKind.isDerivedReference() && bKind.isDerivedReference() && aKind.getDerivedReferenceBase().equals(bKind.getDerivedReferenceBase())) {
                resultKind = LIRKind.value(a.getPlatformKind());
            } else {
                resultKind = aKind.makeUnknownReference();
            }
        } else {
            resultKind = LIRKind.combine(a, b);
        }

        return emitSub(resultKind, a, b, setFlags);
    }
}
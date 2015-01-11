/*
 * Copyright 2015, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.smalidea.dexlib;

import com.google.common.collect.Lists;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.util.ReferenceUtil;
import org.jf.smalidea.psi.impl.SmaliClass;
import org.jf.smalidea.psi.impl.SmaliFile;
import org.jf.smalidea.psi.impl.SmaliMethod;
import org.junit.Assert;

import java.util.List;

public class SmalideaMethodTest extends LightCodeInsightFixtureTestCase {

    public void testSmalideaMethod() {
        String text = ".class public Lmy/pkg/blah; .super Ljava/lang/Object;\n" +
                ".method public someMethodName(I)I\n" +
                "    .registers 4\n" +
                "    .param p1, \"edge\"    # I\n" +

                "    goto :here  #0: 10t\n" +
                "    :here\n" +
                "    return-void  #1: 21c\n" +
                "    const/4 v0, 1234 #2: 11n\n" +
                "    monitor-enter v1, #3: 11x\n" +
                "    move v1, v0 #4: 12x\n" +
                "    goto/16 :here #5: 20t\n" +
                "    sget v0, La/b/c;->blah:I #6: 21c\n" +
                "    const/high16 v0, 0x12340000 #7: 21ih\n" +
                "    const-wide/high16 v0, 0x1234000000000000L #8: 21lh\n" +
                "    const-wide/16 v0, 1234 #9: 21s\n" +
                "    if-eqz v0, :here #10: 21t\n" +
                "    add-int/lit8 v0, v1, 123 #11: 22b\n" +
                "    iget v1, v2, Labc;->blort:Z #12: 22c\n" +
                "    add-int/lit16 v0, v1, 1234 #13: 22s\n" +
                "    if-eq v0, v1, :here #14: 22t\n" +
                "    move/from16 v0, v1 #15: 22x\n" +
                "    cmpl-float v0, v1, v2 #16: 23x\n" +
                "    goto/32 :here #17: 30t\n" +
                "    const-string/jumbo v0, \"abcd\" #18: 31c\n" +
                "    const v0, 1234 #19: 31i\n" +
                "    move/16 v0, v1 #20: 32x\n" +
                "    invoke-virtual {v0, v1, v2, v3, v4}, Lblah;->blort(IIII)I #21: 35c\n" +
                "    invoke-virtual/range {v0..v4}, Lblah;->blort(IIII)I #22: 3rc\n" +
                "    const-wide v0, 0x1234567890L #23: 51i\n" +
                ".end method";

        SmaliFile file = (SmaliFile)myFixture.addFileToProject("my/pkg/blah.smali", text);
        SmaliClass smaliClass = file.getPsiClass();
        SmaliMethod smaliMethod = smaliClass.getMethods()[0];

        SmalideaMethod method = new SmalideaMethod(smaliMethod);
        Assert.assertEquals("Lmy/pkg/blah;", method.getDefiningClass());
        Assert.assertEquals("someMethodName", method.getName());
        Assert.assertEquals("I", method.getReturnType());

        List<? extends CharSequence> parameterTypes = method.getParameterTypes();
        Assert.assertEquals(1, parameterTypes.size());
        Assert.assertEquals("I", parameterTypes.get(0));

        List<? extends MethodParameter> parameters = method.getParameters();
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals("I", parameters.get(0).getType());
        Assert.assertEquals("edge", parameters.get(0).getName());

        Assert.assertEquals(AccessFlags.PUBLIC.getValue(), method.getAccessFlags());

        MethodImplementation impl = method.getImplementation();
        Assert.assertNotNull(impl);

        Assert.assertEquals(4, impl.getRegisterCount());

        List<? extends Instruction> instructions = Lists.newArrayList(impl.getInstructions());

        {
            Instruction10t instruction = (Instruction10t)instructions.get(0);
            Assert.assertEquals(Opcode.GOTO, instruction.getOpcode());
            Assert.assertEquals(1, instruction.getCodeOffset());
        }

        {
            Instruction10x instruction = (Instruction10x)instructions.get(1);
            Assert.assertEquals(Opcode.RETURN_VOID, instruction.getOpcode());
        }

        {
            Instruction11n instruction = (Instruction11n)instructions.get(2);
            Assert.assertEquals(Opcode.CONST_4, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1234, instruction.getNarrowLiteral());
        }

        {
            Instruction11x instruction = (Instruction11x)instructions.get(3);
            Assert.assertEquals(Opcode.MONITOR_ENTER, instruction.getOpcode());
            Assert.assertEquals(1, instruction.getRegisterA());
        }

        {
            Instruction12x instruction = (Instruction12x)instructions.get(4);
            Assert.assertEquals(Opcode.MOVE, instruction.getOpcode());
            Assert.assertEquals(1, instruction.getRegisterA());
            Assert.assertEquals(0, instruction.getRegisterB());
        }

        {
            Instruction20t instruction = (Instruction20t)instructions.get(5);
            Assert.assertEquals(Opcode.GOTO_16, instruction.getOpcode());
            Assert.assertEquals(-4, instruction.getCodeOffset());
        }

        {
            Instruction21c instruction = (Instruction21c)instructions.get(6);
            Assert.assertEquals(Opcode.SGET, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals("La/b/c;->blah:I", ReferenceUtil.getFieldDescriptor(
                    (FieldReference)instruction.getReference()));
        }

        {
            Instruction21ih instruction = (Instruction21ih)instructions.get(7);
            Assert.assertEquals(Opcode.CONST_HIGH16, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(0x1234, instruction.getHatLiteral());
            Assert.assertEquals(0x12340000, instruction.getNarrowLiteral());
            Assert.assertEquals(0x12340000, instruction.getWideLiteral());
        }

        {
            Instruction21lh instruction = (Instruction21lh)instructions.get(8);
            Assert.assertEquals(Opcode.CONST_WIDE_HIGH16, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(0x1234, instruction.getHatLiteral());
            Assert.assertEquals(0x1234000000000000L, instruction.getWideLiteral());
        }

        {
            Instruction21s instruction = (Instruction21s)instructions.get(9);
            Assert.assertEquals(Opcode.CONST_WIDE_16, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1234, instruction.getWideLiteral());
        }

        {
            Instruction21t instruction = (Instruction21t)instructions.get(10);
            Assert.assertEquals(Opcode.IF_EQZ, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(-14, instruction.getCodeOffset());
        }

        {
            Instruction22b instruction = (Instruction22b)instructions.get(11);
            Assert.assertEquals(Opcode.ADD_INT_LIT8, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1, instruction.getRegisterB());
            Assert.assertEquals(123, instruction.getNarrowLiteral());
        }

        {
            Instruction22c instruction = (Instruction22c)instructions.get(12);
            Assert.assertEquals(Opcode.IGET, instruction.getOpcode());
            Assert.assertEquals(1, instruction.getRegisterA());
            Assert.assertEquals(2, instruction.getRegisterB());
            Assert.assertEquals("Labc;->blort:Z", ReferenceUtil.getFieldDescriptor(
                    (FieldReference)instruction.getReference()));
        }

        {
            Instruction22s instruction = (Instruction22s)instructions.get(13);
            Assert.assertEquals(Opcode.ADD_INT_LIT16, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1, instruction.getRegisterB());
            Assert.assertEquals(1234, instruction.getNarrowLiteral());
        }

        {
            Instruction22t instruction = (Instruction22t)instructions.get(14);
            Assert.assertEquals(Opcode.IF_EQ, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1, instruction.getRegisterB());
            Assert.assertEquals(-22, instruction.getCodeOffset());
        }

        {
            Instruction22x instruction = (Instruction22x)instructions.get(15);
            Assert.assertEquals(Opcode.MOVE_FROM16, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1, instruction.getRegisterB());
        }

        {
            Instruction23x instruction = (Instruction23x)instructions.get(16);
            Assert.assertEquals(Opcode.CMPL_FLOAT, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1, instruction.getRegisterB());
            Assert.assertEquals(2, instruction.getRegisterC());
        }

        {
            Instruction30t instruction = (Instruction30t)instructions.get(17);
            Assert.assertEquals(Opcode.GOTO_32, instruction.getOpcode());
            Assert.assertEquals(-28, instruction.getCodeOffset());
        }

        {
            Instruction31c instruction = (Instruction31c)instructions.get(18);
            Assert.assertEquals(Opcode.CONST_STRING_JUMBO, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals("abcd", ((StringReference)instruction.getReference()).getString());
        }

        {
            Instruction31i instruction = (Instruction31i)instructions.get(19);
            Assert.assertEquals(Opcode.CONST, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1234, instruction.getNarrowLiteral());
        }

        {
            Instruction32x instruction = (Instruction32x)instructions.get(20);
            Assert.assertEquals(Opcode.MOVE_16, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(1, instruction.getRegisterB());
        }

        {
            Instruction35c instruction = (Instruction35c)instructions.get(21);
            Assert.assertEquals(Opcode.INVOKE_VIRTUAL, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterC());
            Assert.assertEquals(1, instruction.getRegisterD());
            Assert.assertEquals(2, instruction.getRegisterE());
            Assert.assertEquals(3, instruction.getRegisterF());
            Assert.assertEquals(4, instruction.getRegisterG());
            Assert.assertEquals("Lblah;->blort(IIII)I", ReferenceUtil.getReferenceString(instruction.getReference()));
        }

        {
            Instruction3rc instruction = (Instruction3rc)instructions.get(22);
            Assert.assertEquals(Opcode.INVOKE_VIRTUAL_RANGE, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getStartRegister());
            Assert.assertEquals(5, instruction.getRegisterCount());
            Assert.assertEquals("Lblah;->blort(IIII)I", ReferenceUtil.getReferenceString(instruction.getReference()));
        }

        {
            Instruction51l instruction = (Instruction51l)instructions.get(23);
            Assert.assertEquals(Opcode.CONST_WIDE, instruction.getOpcode());
            Assert.assertEquals(0, instruction.getRegisterA());
            Assert.assertEquals(0x1234567890L, instruction.getWideLiteral());
        }
    }
}

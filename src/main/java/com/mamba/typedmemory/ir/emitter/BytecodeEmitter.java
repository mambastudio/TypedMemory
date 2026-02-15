/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.ir.emitter;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public class BytecodeEmitter implements CodeEmitter{
    private final CodeBuilder builder;
    
    public BytecodeEmitter(CodeBuilder builder){
        this.builder = builder;
    }

    @Override
    public void iconst(int v) {
        this.iconst(builder, v);
    }

    @Override
    public void aload0() {
        builder.aload(0);
    }

    @Override
    public void aload1() {
        builder.aload(1);
    }

    @Override
    public void astore(int slot) {
        builder.astore(slot);
    }

    @Override
    public void aastore() {
        builder.aastore();
    }

    @Override
    public void iastore() {
        builder.iastore();
    }

    @Override
    public void ldc(String s) {
        builder.ldc(s);
    }

    @Override
    public void ldc2(long l) {
        builder.ldc(l);
    }

    @Override
    public void putstatic(ClassDesc owner, String name, ClassDesc type) {
        builder.putstatic(owner, name, type);
    }

    @Override
    public void getstatic(ClassDesc owner, String name, ClassDesc type) {
        builder.getstatic(owner, name, type);
    }

    @Override
    public void invokeinterface(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokeinterface(owner, name, methodDesc);
    }

    @Override
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokestatic(owner, name, methodDesc);
    }

    @Override
    public void invokespecial(ClassDesc owner, String name, MethodTypeDesc methodDesc) {
        builder.invokespecial(owner, name, methodDesc);
    }

    @Override
    public void putfield(ClassDesc owner, String name, ClassDesc fieldType) {
        builder.putfield(owner, name, fieldType);
    }

    @Override
    public void dup() {
        builder.dup();
    }

    @Override
    public void anewarray(ClassDesc className) {
        builder.anewarray(className);
    }

    @Override
    public void return_() {
        builder.return_();
    }
    
    
    private void iconst(CodeBuilder cb, int value) {
        switch (value) {
            case -1 -> cb.iconst_m1();
            case 0  -> cb.iconst_0();
            case 1  -> cb.iconst_1();
            case 2  -> cb.iconst_2();
            case 3  -> cb.iconst_3();
            case 4  -> cb.iconst_4();
            case 5  -> cb.iconst_5();
            default -> {
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    cb.bipush(value);
                } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    cb.sipush(value);
                } else {
                    cb.ldc(value);
                }
            }
        }
    }
    
}

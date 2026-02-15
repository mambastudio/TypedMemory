/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.ir.emitter;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

/**
 *
 * @author joemw
 */
public interface CodeEmitter {
    public void iconst(int v); 
    public void aload0();
    public void aload1();
    public void astore(int slot);
    public void aastore();
    public void iastore();
    public void ldc(String s);
    public void ldc2(long l);
    public void putstatic(ClassDesc owner, String name, ClassDesc type);
    public void getstatic(ClassDesc owner, String name, ClassDesc type);
    public void invokeinterface(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void invokestatic(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void invokespecial(ClassDesc owner, String name, MethodTypeDesc methodDesc);
    public void putfield(ClassDesc owner, String name, ClassDesc fieldType);
    public void dup();
    public void anewarray(ClassDesc className);
    public void return_();
}

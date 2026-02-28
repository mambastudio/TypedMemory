/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.ir;

import com.mamba.typedmemory.core.Mem;
import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemoryLayout;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemorySegment;
import com.mamba.typedmemory.ir.emitter.BytecodeEmitter;
import com.mamba.typedmemory.ir.lowering.MemLayoutLowering;
import com.mamba.typedmemory.ir.lowering.RecordGetLowering;
import com.mamba.typedmemory.ir.lowering.RecordSetLowering;
import com.mamba.typedmemory.ir.lowering.VarHandleLowering;
import java.lang.classfile.ClassFile;
import static java.lang.classfile.ClassFile.ACC_BRIDGE;
import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STATIC;
import static java.lang.classfile.ClassFile.ACC_SYNTHETIC;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.CLASS_INIT_NAME;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;

/**
 *
 * @author joemw
 */
public class TypedMemoryClassGenerator {
    public static byte[] generate(ClassDesc owner, Class<? extends Record> record, MemLayout memLayout){
        var recordDesc = ClassDesc.ofDescriptor(record.descriptorString());
        var memLayoutString = MemLayoutString.of(memLayout);
        return ClassFile.of().build(owner, 
            b -> {
                b.withFlags(0);
                b.withInterfaceSymbols(ClassDesc.ofDescriptor(Mem.class.descriptorString()));
                b.withField("segment", CD_MemorySegment, ACC_PRIVATE | ACC_FINAL);      
                b.withField("layout", CD_MemoryLayout, ACC_PRIVATE | ACC_STATIC | ACC_FINAL);                    
                for(var name : memLayoutString.varHandleNames())
                    b.withField(name, CD_VarHandle, ACC_PRIVATE | ACC_STATIC | ACC_FINAL); //initialise static fields
                    
                b.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, CD_MemorySegment), ACC_PUBLIC, 
                    b0 -> {
                        var init = Stmt.Block.voidReturn(
                            new Stmt.SimpleStmt(cb ->{
                                cb.aload(0);
                                cb.invokespecial(ConstantDescs.CD_Object, INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void));
                                cb.aload(0);                            
                                cb.aload(1);
                                cb.putfield(owner, "segment", IRHelper.CD_MemorySegment);
                            })
                        );
                        init.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody(CLASS_INIT_NAME, MethodTypeDesc.of(CD_void), ACC_STATIC, 
                    b0 -> {
                        var clinit = Stmt.Block.voidReturn(
                            MemLayoutLowering.lower(memLayout, owner),
                            VarHandleLowering.lower(memLayout, owner)
                        );
                        clinit.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody("get", MethodTypeDesc.of(recordDesc, CD_long), ACC_PUBLIC | ACC_FINAL, 
                    b0 ->{
                        var get = Stmt.Block.RefReturn(
                                new RecordGetLowering().emitGet(owner, record, memLayout)
                        );
                        get.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody("get", MethodTypeDesc.of(ConstantDescs.CD_Object, CD_long), ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
                    cb -> {
                        cb.aload(0);
                        cb.lload(1);
                        cb.invokevirtual(owner, "get",
                            MethodTypeDesc.of(recordDesc, CD_long));
                        cb.areturn();
                    }
                );
                
                b.withMethodBody("set", MethodTypeDesc.of(CD_void, recordDesc, CD_long), ACC_PUBLIC | ACC_FINAL, 
                    b0 ->{
                        var set = Stmt.Block.voidReturn(
                                new RecordSetLowering().emitSet(owner, record, memLayout)
                        );
                        set.emit(new BytecodeEmitter(b0));
                    }
                );
                
                b.withMethodBody("set", MethodTypeDesc.of(CD_void, ConstantDescs.CD_Object, CD_long), ACC_PUBLIC | ACC_BRIDGE | ACC_SYNTHETIC,
                    cb -> {
                        cb.aload(0);
                        cb.aload(1);
                        cb.checkcast(recordDesc);
                        cb.lload(2);
                        cb.invokevirtual(owner, "set",
                            MethodTypeDesc.of(CD_void, recordDesc, CD_long));
                        cb.return_();
                    }
                );
            }
        );
    }
    
    public static ClassDesc generateHiddenImplName(Class<?> valueType) {
        var pkg = valueType.getPackageName();
        var internalPkg = pkg.replace('.', '/');

        var simple =
            "Mem$" + valueType.getSimpleName()
            + "_Impl_"
            + Long.toHexString(System.nanoTime());

        var internalName =
            internalPkg.isEmpty()
                ? simple
                : internalPkg + "/" + simple;

        return ClassDesc.ofInternalName(internalName);
    }
}

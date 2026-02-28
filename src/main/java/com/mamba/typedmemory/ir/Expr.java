/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.ir;

import com.mamba.typedmemory.ir.emitter.CodeEmitter;
import static com.mamba.typedmemory.ir.IRHelper.*;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.ValueLayout;
import java.util.List;

/**
 *
 * @author joemw
 */
public interface Expr {
           
    record WithNameExpr(Expr target, String name, ClassDesc receiverType) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            target.emit(out);     // receiver first
            out.ldc(name);        // argument after
            out.invokeinterface(receiverType, "withName", MethodTypeDesc.of(receiverType, CD_String));
        }
    }
    
    record StructLayoutExpr(ArrayExpr layoutsArray) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutsArray.emit(out);
            out.invokestatic(CD_MemoryLayout, "structLayout", MethodTypeDesc.of(CD_StructLayout, CD_MemoryLayout.arrayType()), true);
        }
    }
    
    record GetStaticExpr(ClassDesc owner, String fieldName,  ClassDesc fieldDesc) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.getstatic(owner, fieldName, fieldDesc);
        }
    }
    
    record VarHandleExpr(Expr layoutExpr, ArrayExpr pathElements) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            layoutExpr.emit(out);      // push receiver
            pathElements.emit(out);
            out.invokeinterface(CD_MemoryLayout, "varHandle", MethodTypeDesc.of(CD_VarHandle, CD_PathElement.arrayType()));
        }        
    }
    
    sealed interface PathElementExpr extends Expr{
        record GroupElementExpr(String name) implements PathElementExpr {
            @Override
            public void emit(CodeEmitter out) {
                out.ldc(name);
                out.invokestatic(CD_PathElement, "groupElement", MethodTypeDesc.of(CD_PathElement, CD_String), true);
            }
        }
        
        record SequenceElementExpr() implements PathElementExpr {
            @Override
            public void emit(CodeEmitter out) {
                out.invokestatic(CD_PathElement, "sequenceElement", MethodTypeDesc.of(CD_PathElement));
            }
        }
    }
    
    
    record ArrayExpr(NewArrayExpr alloc, ArrayInitExpr init) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            // new MemoryLayout[elements.size()]
            alloc.emit(out);
            // stack: [array]

            init.emit(out);
            // stack: [array]
        }
    }
    
    record NewArrayExpr(ClassDesc elementInternalName, int size) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            out.iconst(size);
            out.anewarray(elementInternalName);
        }        
    }
    
    record ArrayInitExpr(List<Expr> elements) implements Expr{
        @Override
        public void emit(CodeEmitter out) {
            for (int i = 0; i < elements.size(); i++) {
                out.dup();          // preserve array
                out.iconst(i);      // index
                elements.get(i).emit(out); // value
                out.aastore();
            }
            // array remains on stack
        }     
    }
        
    record ValueLayoutExpr(ValueLayout layout) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            String fieldName = IRHelper.valueLayoutConstant(layout);
            ClassDesc fieldDesc = IRHelper.valueLayoutClassDesc(layout);

            out.getstatic(
                IRHelper.CD_ValueLayout,
                fieldName,
                fieldDesc
            );
        }
    }
    
    record PaddingLayoutExpr(long size) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.ldc2(size);
            out.invokestatic(CD_MemoryLayout, "paddingLayout", MethodTypeDesc.of(CD_MemoryLayout, CD_long));
        }
    }


    void emit(CodeEmitter out);
}

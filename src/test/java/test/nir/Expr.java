/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.nir;

/**
 *
 * @author joemw
 */
public interface Expr {
    
    record IntConst(int value) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.iconst(value);   // pushes one value
        }
    }
    
    record LoadLocal(int slot) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            out.aload(slot);     // pushes one value
        }
    }
    
    record NewIntArray(Expr length) implements Expr {
        @Override
        public void emit(CodeEmitter out) {
            length.emit(out);    // pushes length
            out.newarrayInt();   // pops length, pushes array
        }
    }
    
    void emit(CodeEmitter out);
}

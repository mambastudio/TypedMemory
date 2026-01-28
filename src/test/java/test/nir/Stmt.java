/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.nir;

import java.util.List;

/**
 *
 * @author joemw
 */
public interface Stmt {
    record StoreLocal(int slot, Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            value.emit(out);     // pushes value
            out.astore(slot);    // pops value
        }
    }
    
    record ArrayStore(Expr array, Expr index, Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            array.emit(out);     // arrayref
            index.emit(out);     // index
            value.emit(out);     // value
            out.iastore();       // pops all three
        }
    }
    
    record Return(Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            value.emit(out);     // pushes return value
            out.areturn();       // pops and exits
        }
    }
    
    record Block(List<Stmt> statements) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            for (Stmt s : statements) {
                s.emit(out);
            }
        }
    }
    
    void emit(CodeEmitter out);
}

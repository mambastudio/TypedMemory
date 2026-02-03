/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package test.mir;

import java.lang.constant.ClassDesc;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author joemw
 */
public interface Stmt {
    record Block(List<Stmt> statements) implements Stmt {
        public static Block clinit(Stmt... stmts) {
            return new Block(
                Stream.concat(
                    Arrays.stream(stmts),
                    Stream.of(new ReturnVoid()) //clinit has returnvoid always
                ).toList()
            );
        }
        
        @Override
        public void emit(CodeEmitter out) {
            for (Stmt s : statements) {
                switch(s){ //flatten (notice it's recursive if it's a mixture of blocks)
                    case Block b -> b.emit(out);
                    case Stmt st -> st.emit(out); //adding interface makes it exhaustive
                }
            }
        }
    }
    
    record PutStatic(ClassDesc owner, String fieldName, ClassDesc fieldDesc, Expr value) implements Stmt {
        @Override
        public void emit(CodeEmitter out) {
            value.emit(out);              // push value
            out.putstatic(owner, fieldName, fieldDesc); // consume value
        }
    }
    
    record ReturnVoid() implements Stmt{

        @Override
        public void emit(CodeEmitter out) {
            out.return_();
        }
        
    }
    void emit(CodeEmitter out);
}

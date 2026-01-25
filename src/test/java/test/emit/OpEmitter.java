/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.emit;

import java.lang.classfile.CodeBuilder;
import test.ir.Op;

/**
 *
 * @author joemw
 */
public class OpEmitter {
    public static void emit(CodeBuilder b, Op op) {
        switch (op) {
            case Op.PutStatic p -> {
                CodeEmitter.emit(b, p.value());   // Expr → value on stack
                b.putstatic(p.owner(), p.name(), p.type());
            }
            case Op.Return _ ->
                b.return_();
        }
    }
}

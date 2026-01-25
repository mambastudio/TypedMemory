/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.ir;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

/**
 *
 * @author joemw
 */
public sealed interface Expr {
    
    enum InvokeKind {STATIC, VIRTUAL, INTERFACE, SPECIAL}
    
    record GetStatic(ClassDesc owner, String name, ClassDesc type) implements Expr {}
    record Call(InvokeKind kind, ClassDesc owner, String name, MethodTypeDesc type, Expr... args) implements Expr {}
    record ArrayLiteral(ClassDesc component, List<Expr> elements) implements Expr {}
    record StringLiteral(String value) implements Expr {}
    record LongLiteral(long value) implements Expr {}
}

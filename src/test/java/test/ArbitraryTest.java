/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.Objects;

/**
 *
 * @author joemw
 */
public class ArbitraryTest {
    void main(){
        
        test(char.class);
    }
    
    void test(Class classType){       
        Objects.requireNonNull(classType);
        switch (classType) {
            case Class<?> c when c == long.class   -> IO.println("long");
            case Class<?> c when c == double.class -> IO.println("double");
            case Class<?> c when c == float.class  -> IO.println("float");
            case Class<?> c when c.isPrimitive()   -> IO.println("int like");
            default -> IO.println("reference");
        }    
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 *
 * @author joemw
 */
public class DummyStruct {
    private final static MemoryLayout layout = MemoryLayout.structLayout(
                                                    ValueLayout.JAVA_BYTE.withName("x"),
                                                    MemoryLayout.paddingLayout(3),
                                                    ValueLayout.JAVA_INT.withName("y"),
                                                    ValueLayout.JAVA_LONG.withName("z")
                                                ).withName("Point");
    
}

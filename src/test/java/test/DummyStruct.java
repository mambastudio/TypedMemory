/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

/**
 *
 * @author joemw
 */
public class DummyStruct {
    public record Point(int x, int y){}
    private static final MemoryLayout layout = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("x"),
                ValueLayout.JAVA_INT.withName("y")
            ).withName("Point");
    
    public static final VarHandle xPointStructLayoutImplHandle = layout.varHandle(PathElement.groupElement("x"));
    public static final VarHandle yPointStructLayoutImplHandle = layout.varHandle(PathElement.groupElement("y"));
    
    private final MemorySegment segment;
    
    public DummyStruct(MemorySegment segment){
        this.segment = segment;
    }
    
    public void set(Point t, long index){
        xPointStructLayoutImplHandle.set(this.segment, index, t.x());
        yPointStructLayoutImplHandle.set(this.segment, index, t.y());
        
      
    }
    
    public Point get(long index){
        int x = (int) xPointStructLayoutImplHandle.get(this.segment, index);
        int y = (int) yPointStructLayoutImplHandle.get(this.segment, index);
        return new Point(x, y);
    }
}

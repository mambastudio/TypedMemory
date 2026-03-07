/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.api.Mem;
import com.mamba.typedmemory.api.MemLayout;
import com.mamba.typedmemory.api.size;
import com.mamba.typedmemory.internal.emitter.DebugEmitter;
import com.mamba.typedmemory.internal.ir.RecordSetLowering;
import com.mamba.typedmemory.internal.layout.MemLayoutString;
import java.lang.constant.ClassDesc;
import java.lang.foreign.Arena;

/**
 *
 * @author joemw
 */
public class TestLayout {
    void main(){
        test4();
    }
    
    public void test1(){
        record Point(int x, int y){}        
        
        MemLayout mL = MemLayout.of(Point.class);       
        MemLayoutString mLS = MemLayoutString.of(mL);
        IO.println(mL);
        IO.println(mLS.varHandleNames());
        mLS.varHandleFields().forEach(string -> IO.println(string));
    }
    
    public void test2(){
        record Point(byte x, @size(3)int[] y){}        
        
        MemLayout mL = MemLayout.of(Point.class);       
        MemLayoutString mLS = MemLayoutString.of(mL);
        IO.println(mL);
        IO.println(mLS.varHandleNames());
    }
    
    public void test3(){
        record Point(int x, int y){}
        MemLayout mL = MemLayout.of(Point.class);
        var recordLower = new RecordSetLowering();
        var stmt = recordLower.emitSet(ClassDesc.ofDescriptor(TestLayout.class.descriptorString()),Point.class, mL);
        stmt.emit(new DebugEmitter());
    }
    
    
    public void test4(){
        record Student(int id, int score, boolean active){}
        try (Arena arena = Arena.ofConfined()) {
            var students = Mem.of(Student.class, arena, 100_000_000);
            IO.println(MemLayout.memorySummary(students));
            IO.println();
            IO.println(MemLayout.describe(Student.class));
        }
    }
    
}

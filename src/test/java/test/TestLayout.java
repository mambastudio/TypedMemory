/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import com.mamba.typedmemory.annotation.size;
import com.mamba.typedmemory.ir.emitter.DebugEmitter;
import com.mamba.typedmemory.ir.lowering.RecordGetLowering;
import java.lang.constant.ClassDesc;

/**
 *
 * @author joemw
 */
public class TestLayout {
    void main(){
        test3();
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
        var recordLower = new RecordGetLowering();
        var stmt = recordLower.reconstructRecord(ClassDesc.ofDescriptor(TestLayout.class.descriptorString()),Point.class, mL);
        stmt.emit(new DebugEmitter());
    }
}

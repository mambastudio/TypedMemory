/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.api.Mem;
import java.lang.foreign.Arena;
import java.util.Random;

/**
 *
 * @author joemw
 */
public class TestIR {
    void main() throws Exception{
        test();
       
    }
        
    void test(){    
        try (Arena arena = Arena.ofConfined()) {
            long size = 10;
            record Color(float r, float g, float b){}
            
            Random random = new Random();
            
            var colors = Mem.of(Color.class, arena, size)
                    .init(()-> new Color(random.nextFloat(0, 1),
                                         random.nextFloat(0, 1),
                                         random.nextFloat(0, 1)));
            colors.query().forEach(IO::println);
            
            IO.println("FILTERED: ");
            
            colors.query()
                    .filter(c -> c.r > 0.5f)
                    .forEach(IO::println);
            
            IO.println(colors.segment());
        }        
    }    
}

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
        test2();
       
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
            
         //   IO.println(colors.segment());
        }        
    } 
  
    void test2(){
        
        
        try (Arena arena = Arena.ofConfined()) {
            record Color(float r, float g, float b) {}
            int size = 10;
            Random random = new Random();

            // allocate memory
            Mem<Color> colors = Mem.of(Color.class, arena, size);

            // fill memory
            for (int i = 0; i < size; i++) {
                colors.set(i, new Color(
                        random.nextFloat(),
                        random.nextFloat(),
                        random.nextFloat()));
            }

            IO.println("ALL COLORS");
            colors.query()
                  .forEach(IO::println);

            IO.println("\nFILTERED (r > 0.5)");
            colors.query()
                  .filter(c -> c.r() > 0.5f)
                  .forEach(IO::println);

            IO.println("\nCOLLECTED (r > 0.5)");
           
            Mem<Color> filtered =
                    colors.query()
                          .filter(c -> c.r() > 0.5f)
                          .collect(arena);
                          
            filtered.query().forEach(IO::println);
        }
    }
}

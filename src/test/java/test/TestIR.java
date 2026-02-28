/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.core.Mem;
import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.ir.TypedMemoryClassGenerator;
import java.lang.constant.ClassDesc;
import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 *
 * @author joemw
 */
public class TestIR {
    record Point(int x, int y) {}
    void main() throws Exception{
        test2();
       
    }
    
    void test1() throws Exception{
        
        var memLayout = MemLayout.of(Point.class);         
        var owner = ClassDesc.of("test.StructType");
        var classBytes = TypedMemoryClassGenerator.generate(owner, Point.class, memLayout);
        
        var testClassesRoot = Path.of("target/test-classes");
        writeClass(owner, classBytes, testClassesRoot);
    }
    
    
    void test2(){
        
        
        try (Arena arena = Arena.ofConfined()) {
            long size = 10_000_000;
            Random random = new Random();
            Mem<Point> points = Mem.of(Point.class, arena, size);
            for(int i = 0; i<size; i++){
                points.set(new Point(random.nextInt(), random.nextInt()), i);
                
            }
        }
        
    }
    
    void writeClass(ClassDesc classDesc, byte[] classBytes, Path classesRoot)
        throws Exception {
               
        var pkg = classDesc.packageName();
        var dir = pkg.isEmpty()
                ? classesRoot
                : classesRoot.resolve(pkg.replace('.', '/'));

        var path = dir.resolve(classDesc.displayName() + ".class");
        Files.write(path, classBytes);
    }
}

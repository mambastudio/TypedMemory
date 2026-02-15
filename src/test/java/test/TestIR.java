/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.ir.lowering.VarHandleLowering;
import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import com.mamba.typedmemory.ir.IRHelper;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemoryLayout;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemorySegment;
import java.lang.constant.ClassDesc;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;
import com.mamba.typedmemory.ir.Stmt.Block;
import com.mamba.typedmemory.ir.Stmt.SimpleStmt;
import com.mamba.typedmemory.ir.emitter.BytecodeEmitter;
import com.mamba.typedmemory.ir.lowering.MemLayoutLowering;
import com.mamba.typedmemory.ir.emitter.DebugEmitter;
import java.lang.classfile.ClassFile;
import static java.lang.classfile.ClassFile.ACC_FINAL;
import static java.lang.classfile.ClassFile.ACC_PRIVATE;
import static java.lang.classfile.ClassFile.ACC_PUBLIC;
import static java.lang.classfile.ClassFile.ACC_STATIC;
import java.lang.constant.ConstantDescs;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.lang.constant.ConstantDescs.CLASS_INIT_NAME;
import static java.lang.constant.ConstantDescs.INIT_NAME;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author joemw
 */
public class TestIR {
    void main() throws Exception{
        test1();
       
    }
    
    void test1() throws Exception{
        record Point(int x, int y){}
        
        var memLayout = MemLayout.of(Point.class);      
        var memLayoutString = MemLayoutString.of(memLayout);        
        var owner = ClassDesc.of("test.StructType");
        
        var classBytes = ClassFile.of().build(owner, 
                b -> {
                    b.withFlags(0);
                    b.withField("segment", CD_MemorySegment, ACC_PRIVATE | ACC_FINAL);      
                    b.withField("layout", CD_MemoryLayout, ACC_PRIVATE | ACC_STATIC | ACC_FINAL);                    
                    for(var name : memLayoutString.varNames())
                        b.withField(name, CD_VarHandle, ACC_PRIVATE | ACC_STATIC | ACC_FINAL); //initialise static fields
                    
                    b.withMethodBody(INIT_NAME, MethodTypeDesc.of(CD_void, CD_MemorySegment), ACC_PUBLIC, 
                        b0 -> {
                            var init = Block.voidReturn(
                                    new SimpleStmt(cb ->{
                                        cb.aload0();
                                        cb.invokespecial(ConstantDescs.CD_Object, INIT_NAME, MethodTypeDesc.of(ConstantDescs.CD_void));
                                        cb.aload0();                            
                                        cb.aload1();
                                        cb.putfield(owner, "segment", IRHelper.CD_MemorySegment);
                                    }));
                            init.emit(new BytecodeEmitter(b0));
                        }
                    );
                    
                    b.withMethodBody(CLASS_INIT_NAME, MethodTypeDesc.of(CD_void), ACC_STATIC, 
                        b0 -> {
                            var clinit = Block.voidReturn(
                                MemLayoutLowering.lower(memLayout, owner),
                                VarHandleLowering.lower(memLayout, owner)
                            );                  
                            clinit.emit(new BytecodeEmitter(b0));
                        }
                    );
                    
                    
                });
        
        var testClassesRoot = Path.of("target/test-classes");
        writeClass(owner, classBytes, testClassesRoot);
    }
    
    
    void test2(){
        var layout = MemoryLayout.structLayout(ValueLayout.JAVA_BYTE.withName("x"),
                                                MemoryLayout.paddingLayout(3),
                                                ValueLayout.JAVA_INT.withName("y"),
                                                MemoryLayout.structLayout(
                                                    ValueLayout.JAVA_INT.withName("x"),
                                                    ValueLayout.JAVA_INT.withName("y")
                                                ).withName("Pixel")
                                            ).withName("Point");
        var memLayout = new MemLayout(layout);
        
        var owner = ClassDesc.of("test.StructType");                        
        var clinit =  Block.voidReturn(
                            MemLayoutLowering.lower(memLayout, owner),
                            VarHandleLowering.lower(memLayout, owner));
        
        var init = Block.voidReturn(
                        new SimpleStmt(b0 ->{
                            b0.aload0();
                            b0.invokespecial(ConstantDescs.CD_Object, "<init>", MethodTypeDesc.of(ConstantDescs.CD_void));
                            b0.aload0();                            
                            b0.aload1();
                            b0.putfield(owner, "segment", IRHelper.CD_MemorySegment);
                        }));
                
        init.emit(new DebugEmitter());
        clinit.emit(new DebugEmitter());        
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

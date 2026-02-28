/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.ir.lowering;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemorySegment;
import static com.mamba.typedmemory.ir.IRHelper.classify;
import com.mamba.typedmemory.ir.RecordVarHandlePlan;
import com.mamba.typedmemory.ir.Stmt;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_void;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joemw
 */
public class RecordSetLowering {
    public Stmt emitSet(ClassDesc owner, Class<? extends Record> recordType, MemLayout memLayout) {

        var plans = buildPlans(recordType, memLayout);

        var stmts = new ArrayList<Stmt>();

        int segmentSlot = 0; // this
        int recordSlot  = 1; // parameter t
        int indexSlot   = 2; // long index

        for (RecordVarHandlePlan plan : plans) 
            stmts.add(emitVarHandleFieldSet(owner, recordType, plan, segmentSlot, recordSlot, indexSlot));

        return new Stmt.Block(stmts);
    }
    
    private Stmt emitVarHandleFieldSet(ClassDesc owner, Class<? extends Record> recordType, RecordVarHandlePlan plan,
        int segmentSlot, int recordSlot, int indexSlot) {
        return new Stmt.SimpleStmt(out ->{
            // getstatic owner.xHandle
            out.getstatic(owner,
                          plan.varHandleFieldName(),
                          CD_VarHandle);
            
            // load segment
            out.aload(segmentSlot);
            out.getfield(owner, "segment", CD_MemorySegment);

            // load index
            out.lload(indexSlot);

            // load record param
            out.aload(recordSlot);
            
            // call accessor
            var accessorName = plan.component().getName();
            var accessorDesc = MethodTypeDesc.of(
                    ClassDesc.ofDescriptor(plan.component().getType().descriptorString()));
            
            out.invokevirtual(ClassDesc.of(recordType.getName()), accessorName, accessorDesc);
            // call VarHandle.set
            out.invokevirtual(CD_VarHandle, "set", plan.vhType());

        });
    }
    
    private List<RecordVarHandlePlan> buildPlans(Class<? extends Record> recordType, MemLayout memLayout) {
        var memLayoutString = MemLayoutString.of(memLayout);  
        
        var varHandleNames = memLayoutString.varHandleNames(); //list of varhandles
        var components = recordType.getRecordComponents();  //list of record components

        var plans = new ArrayList<RecordVarHandlePlan>(); //init plans for varhandles

        for (int i = 0; i < components.length; i++) {
            var component = components[i];
            var vhName = varHandleNames.get(i);

            var jvmType = classify(component.getType()); //for return type of varhandle.get(...) - custom grouping (in enum)
            var returnDesc = ClassDesc.ofDescriptor(component.getType().descriptorString()); ////for return type of varhandle.get(...)

            var vhType = MethodTypeDesc.of(CD_void, CD_MemorySegment, CD_long, returnDesc); //method type for varhandle.get containing parameters and return type          
            plans.add(new RecordVarHandlePlan(component, vhName, jvmType, vhType));
        }

        return plans;
    }
}

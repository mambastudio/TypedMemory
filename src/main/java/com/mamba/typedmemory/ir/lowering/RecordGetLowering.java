/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.ir.lowering;

import com.mamba.typedmemory.core.FieldType;
import com.mamba.typedmemory.core.FieldType.ArrayField;
import com.mamba.typedmemory.core.FieldType.PrimitiveField;
import com.mamba.typedmemory.core.FieldType.RecordField;
import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemorySegment;
import com.mamba.typedmemory.ir.IRHelper.JVMType;
import static com.mamba.typedmemory.ir.IRHelper.JVMType.REFERENCE;
import com.mamba.typedmemory.ir.IRHelper.LocalInfo;
import static com.mamba.typedmemory.ir.IRHelper.classify;
import static com.mamba.typedmemory.ir.IRHelper.constructorTypeDesc;
import static com.mamba.typedmemory.ir.IRHelper.firstFreeSlot;
import com.mamba.typedmemory.ir.LocalSlotAllocator;
import com.mamba.typedmemory.ir.RecordVarHandlePlan;
import com.mamba.typedmemory.ir.Stmt;

import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;

import module java.base;
import static java.lang.constant.ConstantDescs.INIT_NAME;

/**
 *
 * @author joemw
 */
public class RecordGetLowering {
    
    public Stmt reconstructRecord(Class<? extends Record> recordType, MemLayout memLayout){
        var owner = ClassDesc.ofDescriptor(recordType.descriptorString());
        
        var plans = buildPlans(recordType, memLayout);
        
        var slots = new LocalSlotAllocator(firstFreeSlot(false, long.class));
        var locals = new ArrayList<LocalInfo>();
        var stmts = new ArrayList<Stmt>();
        
        var segmentSlot = 0; // instance method: this
        var indexSlot   = 1; // long parameter

        for (RecordVarHandlePlan plan : plans) {
            //Load value via VarHandle
            stmts.add(loadPrimitive(owner, plan, segmentSlot, indexSlot));
            
            // Allocate local
            int slot = slots.allocate(plan.actualType());
            locals.add(new LocalInfo(slot, plan.jvmType()));

            // Store into local
            stmts.add(storePrimitive(slot, plan.jvmType()));
        }
        
        //Construct record from locals
        stmts.add(constructRecord(recordType, locals));

        return new Stmt.Block(stmts);
    }
            
    private Stmt constructRecord(Class<?> recordType, List<LocalInfo> localSlots){
        return new Stmt.SimpleStmt(out -> {
            var desc = ClassDesc.of(recordType.getName());
             
            out.new_(desc);
            out.dup();
            
            for (LocalInfo local : localSlots) {               
                switch (local.type()) {
                    case INT_LIKE -> out.iload(local.slot());
                    case LONG -> out.lload(local.slot());
                    case FLOAT -> out.fload(local.slot());
                    case DOUBLE -> out.dload(local.slot());
                    case REFERENCE -> out.aload(local.slot());
                }
            }
            
            out.invokespecial(desc, INIT_NAME, constructorTypeDesc(recordType));
        });
    }
        
    private Stmt storePrimitive(int slot, JVMType type) {
        return new Stmt.SimpleStmt(out -> {
            switch (type) {
                case INT_LIKE -> out.storeLocal(TypeKind.INT, slot);
                case LONG -> out.storeLocal(TypeKind.LONG, slot);
                case FLOAT -> out.storeLocal(TypeKind.FLOAT, slot);
                case DOUBLE -> out.storeLocal(TypeKind.DOUBLE, slot);
                default -> throw new IllegalStateException();
            }
        });
    }
    
    private Stmt loadPrimitive(ClassDesc owner, RecordVarHandlePlan field, int segmentSlot, int indexSlot) {
        //Comments below are for reminding what happens in case I need to rewrite
        return new Stmt.SimpleStmt(out -> {
            //Load static VarHandle field
            out.getstatic(owner, field.varHandleFieldName(), CD_VarHandle);
            
            //Load segment (this.segment)
            out.aload(segmentSlot);  // usually 0
            out.getfield(owner, "segment", CD_MemorySegment);
            
            //Load index (long)
            out.lload(indexSlot);
            
            //Invoke VarHandle.get            
            out.invokevirtual(CD_VarHandle, "get", field.vhType());
        });
    }
    
    public List<RecordVarHandlePlan> buildPlans(Class<? extends Record> recordType, MemLayout memLayout) {
        var memLayoutString = MemLayoutString.of(memLayout);  
        
        var varHandleNames = memLayoutString.varHandleNames(); //list of varhandles
        var components = recordType.getRecordComponents();  //list of record components

        var plans = new ArrayList<RecordVarHandlePlan>(); //init plans for varhandles

        for (int i = 0; i < components.length; i++) {
            var component = components[i];
            var vhName = varHandleNames.get(i);

            var jvmType = classify(component.getType());
            var returnDesc = ClassDesc.ofDescriptor(component.getType().descriptorString());

            var vhType = MethodTypeDesc.of(returnDesc, CD_MemorySegment, CD_long); // or CD_int depending on coordinate           
            plans.add(new RecordVarHandlePlan(component, vhName, jvmType, vhType));
        }

        return plans;
    }
}

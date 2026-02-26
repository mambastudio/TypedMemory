package com.mamba.typedmemory.ir.lowering;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import com.mamba.typedmemory.ir.IRHelper;
import static com.mamba.typedmemory.ir.IRHelper.CD_MemorySegment;
import com.mamba.typedmemory.ir.IRHelper.LocalInfo;
import static com.mamba.typedmemory.ir.IRHelper.classify;
import static com.mamba.typedmemory.ir.IRHelper.firstFreeSlot;
import com.mamba.typedmemory.ir.LocalSlotAllocator;
import com.mamba.typedmemory.ir.RecordVarHandlePlan;
import com.mamba.typedmemory.ir.Stmt;

import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_long;

import module java.base;

/**
 *
 * @author joemw
 */
public class RecordGetLowering {
    
    public Stmt reconstructRecord(ClassDesc owner, Class<? extends Record> recordType, MemLayout memLayout){
        
        //create record build plan which has metainfo on components, varhandlename, return type, get method description for varhandle
        var plans = buildPlans(recordType, memLayout);
        
        var slots = new LocalSlotAllocator(firstFreeSlot(false, long.class));
        var locals = new ArrayList<LocalInfo>();
        var stmts = new ArrayList<Stmt>();
        
        var segmentSlot = 0; // instance method: this
        var indexSlot   = 1; // long parameter of method get in owner ClassDesc

        for (RecordVarHandlePlan plan : plans) {
            //Load value via VarHandle using meta info from plan
            stmts.add(emitVarHandleFieldGet(owner, plan, segmentSlot, indexSlot));
            
            // Allocate local
            int slot = slots.allocate(plan.actualType());
            locals.add(new LocalInfo(slot, plan.jvmType()));

            // Store into local
            stmts.add(
                    new Stmt.SimpleStmt(out -> {
                        IRHelper.emitStore(out, plan.jvmType(), slot);
                    }));
        }
        
        //Construct record from locals
        stmts.add(IRHelper.emitRecordConstructorCall(recordType, locals));

        return new Stmt.Block(stmts);
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

            var vhType = MethodTypeDesc.of(returnDesc, CD_MemorySegment, CD_long); //method type for varhandle.get containing parameters and return type          
            plans.add(new RecordVarHandlePlan(component, vhName, jvmType, vhType));
        }

        return plans;
    }
                                
    private Stmt emitVarHandleFieldGet(ClassDesc owner, RecordVarHandlePlan field, int segmentSlot, int indexSlot) {
        //Comments below are for reminding what happens in case I need to rewrite
        return new Stmt.SimpleStmt(out -> {
            //Load static VarHandle field
            out.getstatic(owner, field.varHandleFieldName(), CD_VarHandle);
            
            //Load segment (this.segment)
            out.aload(segmentSlot);  // actually 0, because it is 'this'
            out.getfield(owner, "segment", CD_MemorySegment);
            
            //Load index (long), the first parameter of method get, hence index slot is 1
            out.lload(indexSlot);
            
            //Invoke VarHandle.get            
            out.invokevirtual(CD_VarHandle, "get", field.vhType());
        });
    }
}

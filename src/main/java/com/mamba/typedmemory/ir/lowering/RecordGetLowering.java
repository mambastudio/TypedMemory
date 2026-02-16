/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.ir.lowering;

import com.mamba.typedmemory.core.FieldType;
import com.mamba.typedmemory.ir.Stmt;
import java.lang.constant.ClassDesc;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joemw
 */
public class RecordGetLowering {
    public Stmt get(Class<?> recordType, ClassDesc owner){
        
        var fieldType = FieldType.of(recordType, recordType.getSimpleName());
        RecordComponent[] components = fieldType.type().getRecordComponents();
        var stmts = new ArrayList<Stmt>();
        return null;
    }
}

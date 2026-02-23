/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.ir;

import com.mamba.typedmemory.ir.IRHelper.JVMType;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.RecordComponent;

/**
 *
 * @author joemw
 */
public record RecordVarHandlePlan(
        RecordComponent component,
        String varHandleFieldName,
        JVMType jvmType,
        MethodTypeDesc vhType) {
    
    public Class<?> actualType(){
        return component.getType();
    }

}

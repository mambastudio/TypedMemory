/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.core;

import com.mamba.typedmemory.core.Mem.MemCache;
import java.lang.foreign.Arena;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joemw
 */
public interface Mem<T> {
    public void set(T t, long index);
    public T get(long index);
    public long address();
    
    public static <T> Mem<T> of(Class<T> clazz, Arena arena, long size){
        if(MemCache.of().containsKey(clazz)){
            if(clazz.isRecord()){
                var mem = MemLayout.of((Class<? extends Record>) clazz);
                var segment = arena.allocate(mem.layout(), size);
            }
        }
        else if(clazz.isInterface() && clazz.isSealed()){
            
        }
        
        throw new UnsupportedOperationException("Should be record or sealed interface");
    }
    
    final class MemCache {
        private MemCache() {}

        private static final Map<Class<?>, MethodHandle> CACHE =
                new ConcurrentHashMap<>();
        
        private static Map<Class<?>, MethodHandle> of(){
            return CACHE;
        }
    }
}

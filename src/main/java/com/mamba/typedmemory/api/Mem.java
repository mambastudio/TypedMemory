/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mamba.typedmemory.api;

import com.mamba.typedmemory.api.Mem.MemCache;
import com.mamba.typedmemory.internal.ir.TypedMemoryClassGenerator;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joemw
 * @param <T>
 */
public interface Mem<T> {
    public void set(long index, T t);
    public T get(long index);
    public MemorySegment segment();
           
    default long address(){
        return segment().address();
    }
    
    public static <T extends Record> Mem<T> of(Class<T> clazz, Arena arena, long size) {
        try {
            var cache = MemCache.of();
            var ctor = cache.get(clazz);
            var lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
            var memLayout = MemLayout.of(clazz);
            
            if (ctor == null) {

                if (!clazz.isRecord())
                    throw new IllegalArgumentException("Must be record");

                var owner = TypedMemoryClassGenerator.generateHiddenImplName(clazz);

                byte[] bytes = TypedMemoryClassGenerator.generate(owner, clazz, memLayout);
                
                var hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
                var hiddenClass = hiddenLookup.lookupClass();

                ctor = hiddenLookup.findConstructor(hiddenClass, MethodType.methodType(void.class, MemorySegment.class));

                ctor = ctor.asType(MethodType.methodType(Mem.class, MemorySegment.class));

                cache.put(clazz, ctor);
            }

            var segment = arena.allocate(memLayout.layout(), size);

            return (Mem<T>) ctor.invoke(segment);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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

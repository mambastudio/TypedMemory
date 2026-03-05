/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.api;

import com.mamba.typedmemory.api.MemQuery.Stage.FilterStage;
import com.mamba.typedmemory.api.MemQuery.Stage.MapStage;
import java.lang.foreign.Arena;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 *
 * @author jmburu
 * @param <T>
 */
public final class MemQuery<T> {
    private final Stage<T> stage;    

    MemQuery(Stage<T> stage) {
        this.stage = stage;
    }

    public MemQuery<T> filter(Predicate<T> predicate) {
        return new MemQuery<>(new FilterStage<>(stage, predicate));
    }

    public MemQuery<T> map(UnaryOperator<T> mapper) {
        return new MemQuery<>(new MapStage<>(stage, mapper));
    }

    public void forEach(Consumer<T> action) {
        stage.forEach(action);
    }
    
    public Mem<T> collect(Arena arena) {
        final long[] count = {0};
        stage.forEach(v -> count[0]++);
        Mem<T> result = stage.source().allocateLike(arena, count[0]);
        final long[] index = {0};
        stage.forEach(v -> result.set(index[0]++, v));
        return result;
    }

        
    sealed interface Stage<T>{
        void forEach(Consumer<T> action);
        Mem<T> source();
                
        record SourceStage<T>(Mem<T> mem) implements Stage<T> {
            @Override
            public void forEach(Consumer<T> action) {
                long n = mem.size();
                for (long i = 0; i < n; i++)
                    action.accept(mem.get(i));
            }
            
            @Override
            public Mem<T> source() {
                return mem;
            }
        }
        
        record FilterStage<T>(Stage<T> prev, Predicate<T> predicate) implements Stage<T> {
            @Override
            public void forEach(Consumer<T> action) {
                prev.forEach(v -> {
                    if (predicate.test(v))
                        action.accept(v);
                });
            }
            
            @Override
            public Mem<T> source() {
                return prev.source();
            }
        }
        
        record MapStage<T>(Stage<T> prev, UnaryOperator<T> mapper) implements Stage<T> {
            @Override
            public void forEach(Consumer<T> action) {
                prev.forEach(v -> action.accept(mapper.apply(v)));
            }
            
            @Override
            public Mem<T> source() {
                return prev.source();
            }
        }
    }
}

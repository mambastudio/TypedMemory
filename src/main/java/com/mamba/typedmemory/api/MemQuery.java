/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.api;

import com.mamba.typedmemory.api.MemQuery.Stage.FilterStage;
import com.mamba.typedmemory.api.MemQuery.Stage.MapStage;
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
    
    sealed interface Stage<T>{
        void forEach(Consumer<T> action);
        
        record SourceStage<T>(Mem<T> mem) implements Stage<T> {
            @Override
            public void forEach(Consumer<T> action) {
                long n = mem.size();
                for (long i = 0; i < n; i++)
                    action.accept(mem.get(i));
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
        }
        
        record MapStage<T>(Stage<T> prev, UnaryOperator<T> mapper) implements Stage<T> {
            @Override
            public void forEach(Consumer<T> action) {
                prev.forEach(v -> action.accept(mapper.apply(v)));
            }
        }
    }
}

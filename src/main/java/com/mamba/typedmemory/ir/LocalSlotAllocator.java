/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mamba.typedmemory.ir;

/**
 *
 * @author joemw
 */
public final class LocalSlotAllocator {

    private int next;

    public LocalSlotAllocator(int firstFreeSlot) {
        this.next = firstFreeSlot;
    }

    public int allocate(Class<?> type) {
        int slot = next;

        if (type == long.class || type == double.class) {
            next += 2;
        } else {
            next += 1;
        }

        return slot;
    }

    public int nextFree() {
        return next;
    }
}


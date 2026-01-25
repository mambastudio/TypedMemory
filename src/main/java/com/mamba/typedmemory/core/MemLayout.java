/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package com.mamba.typedmemory.core;

import com.mamba.typedmemory.core.FieldType.ArrayField;
import com.mamba.typedmemory.core.FieldType.MemSize;
import com.mamba.typedmemory.core.FieldType.PrimitiveField;
import com.mamba.typedmemory.core.FieldType.RecordField;
import static com.mamba.typedmemory.core.MemAnalyser.computeAlignmentOffset;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.reflect.RecordComponent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author user
 */
public record MemLayout(MemoryLayout layout, Optional<List<MemoryLayout>> groupLayouts) implements MemAnalyser{
    public MemLayout{
        Objects.requireNonNull(layout);
        Objects.requireNonNull(groupLayouts);
    }
        
    public boolean hasInnerLayouts(){
       return (groupLayouts.isPresent() && !groupLayouts.get().isEmpty());        
    }
    
    public boolean isSequence(){
        return layout instanceof SequenceLayout;
    }
    
    public boolean isGroup(){
        return layout instanceof GroupLayout;
    }
    
    public GroupLayout groupLayout() {
        return switch (layout) {
            case SequenceLayout seq when seq.elementLayout() instanceof GroupLayout g -> g;
            case GroupLayout g -> g;
            default -> throw new UnsupportedOperationException();
        };
    }
    
    public MemLayout ofSequenceSize(long size){
        return new MemLayout(MemoryLayout.sequenceLayout(size, groupLayout()), groupLayouts());
    }
    
    public Deque<MemoryLayout> groupLayoutsDeque(){
        return new ArrayDeque(groupLayouts.orElseThrow());
    }
    
    public String name(){
        return layout.name().get();
    }
    
    @Override
    public String toString(){
        Objects.requireNonNull(layout);
        return MemLayoutString.of(this).stringLayout();
    }
    
    public static MemLayout ofSequence(Class<? extends Record> clazz, String name, long size){
        if(size < 0)
            throw new UnsupportedOperationException("size should be greater than 0");
        Optional<List<MemoryLayout>> gOptional = Optional.of(new ArrayList());
        MemLayout gL = of((RecordField)FieldType.of(clazz, name), gOptional);        
        return new MemLayout(MemoryLayout.sequenceLayout(size, gL.layout()).withName(name), gL.groupLayouts());        
    }
    
    public static MemLayout of(Class<? extends Record> clazz){
        FieldType type = FieldType.of(clazz, clazz.getSimpleName());
        return of((RecordField)type, Optional.of(new ArrayList()));
    }
    
    private static MemLayout of(RecordField field, Optional<List<MemoryLayout>> groupLayoutLists){        
        MemSize memSize = field.byteSize();
        
        RecordComponent[] components = field.type().getRecordComponents();
        long offset = 0;        
        ArrayList<MemoryLayout> layouts = new ArrayList();    
        
        for (RecordComponent component : components) {
            switch (FieldType.of(component)) {
                case PrimitiveField prim ->{
                    int size = prim.primitiveByteSize();
                    long alignedOffset = computeAlignmentOffset(offset, size);
                                        
                    // Add padding if needed
                    if (alignedOffset > offset) 
                        layouts.add(MemoryLayout.paddingLayout(alignedOffset - offset));
                    
                    // Add field layout
                    layouts.add(prim.valueLayout().withName(prim.name()));
                    
                    // Update the offset
                    offset = alignedOffset + size;
                }
                case RecordField rec -> {
                    // Calculate alignment for the record
                    MemSize rSize = rec.byteSize();
                    long alignedOffset = computeAlignmentOffset(offset, rec.alignByteSize());

                    // Add padding if needed
                    if (alignedOffset > offset) 
                        layouts.add(MemoryLayout.paddingLayout(alignedOffset - offset));
                    
                    // Recursively generate layout for the inner record
                    int indexToInsert = groupLayoutLists.map(List::size).orElse(-1);
                    MemoryLayout groupLayout = of(rec, groupLayoutLists).layout().withName(rec.typeName()); 
                    groupLayoutLists.ifPresent(list-> list.add(indexToInsert, groupLayout));
                    layouts.add(groupLayout.withName(rec.name()));
                    
                    // Update the offset
                    offset = alignedOffset + rSize.endOffset();
                }
                case ArrayField(var name, var _, var componentType, var arrSize) -> {
                    long alignedOffsetArr = 0;
                    long elementSize = 0;
                    MemoryLayout elementLayout;
                    
                    switch (FieldType.of(componentType, name)) {
                        case PrimitiveField p ->{
                            elementSize = p.primitiveByteSize();
                            alignedOffsetArr = computeAlignmentOffset(offset, elementSize);

                            // Add padding if needed
                            if (alignedOffsetArr > offset) 
                                layouts.add(MemoryLayout.paddingLayout(alignedOffsetArr - offset));

                            // Generate sequence layout for primitive arrays
                            elementLayout = p.valueLayout();
                        }
                        case RecordField r ->{
                            // Calculate layout for record elements
                            MemSize rSize = r.byteSize();
                            long recordAlignment = r.alignByteSize();
                            alignedOffsetArr = computeAlignmentOffset(offset, recordAlignment);

                            // Add padding if needed
                            if (alignedOffsetArr > offset) 
                                layouts.add(MemoryLayout.paddingLayout(alignedOffsetArr - offset));
                            
                            // Recursively generate layout for the record elements
                            int indexToInsert = groupLayoutLists.map(List::size).orElse(-1);
                            elementLayout = of(r, groupLayoutLists).layout().withName(r.typeName());
                            groupLayoutLists.ifPresent(list-> list.add(indexToInsert, elementLayout));
                            elementSize = rSize.size(); // Use the record's calculated size
                        } 
                        case ArrayField _ -> throw new UnsupportedOperationException("Array in an array? How did you get here? Please message, because I'm curious how!");
                    }
                    
                    // Add the array layout to the joiner with its name
                    layouts.add(MemoryLayout.sequenceLayout(arrSize, elementLayout).withName(name));
                    
                    // Update the offset
                    if(arrSize == 0) arrSize = 1;
                    offset = alignedOffsetArr + arrSize * elementSize;
                }
            }
        }
        
        // Add final padding if required
        if (memSize.hasPadding()) {
            layouts.add(MemoryLayout.paddingLayout(memSize.padding()));
            
        }
        
        MemoryLayout[] layArray = new MemoryLayout[layouts.size()];
        for(int i = 0; i<layouts.size(); i++)
            layArray[i] = layouts.get(i);
        
        return new MemLayout(MemoryLayout.structLayout(layArray).withName(field.type().getSimpleName()), groupLayoutLists);
    }
}

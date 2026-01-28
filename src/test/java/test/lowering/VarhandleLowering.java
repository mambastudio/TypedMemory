/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.lowering;

import com.mamba.typedmemory.core.MemLayout;
import com.mamba.typedmemory.core.MemLayoutString;
import java.lang.constant.ClassDesc;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import java.lang.constant.MethodTypeDesc;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import test.ir.Expr;

/**
 *
 * @author joemw
 */
public class VarhandleLowering {
    
    private static final ClassDesc CD_MemoryLayout         = ClassDesc.of(MemoryLayout.class.getName());
    private static final ClassDesc CD_PathElement          = ClassDesc.of(PathElement.class.getName());
    
    public static List<Expr> ofExpr(MemLayout memLayout) {
        var memLayoutString = MemLayoutString.of(memLayout);
        var varNames = memLayoutString.varNames();
        return ofExpr(memLayout.layout(), new LinkedList<>(varNames), new ArrayDeque<>());
    }
            
    private static List<Expr> ofExpr(MemoryLayout memLayout, Deque<String> varNames, Deque<Expr> pathStack) {
        List<Expr> result = new ArrayList<>();
        switch (memLayout) {
            case GroupLayout group -> {
                for (MemoryLayout m : group.memberLayouts()) {

                    switch (m) {
                        case ValueLayout v -> {
                            // Build path = current path + this field
                            List<Expr> path = new ArrayList<>(pathStack);
                            path.add(groupElement(v.name().get()));

                            Expr vhExpr = varHandleExpr(path);
                            result.add(vhExpr);

                            varNames.removeFirst(); // consume name
                        }

                        case GroupLayout g -> {
                            pathStack.addLast(groupElement(g.name().get()));
                            result.addAll(ofExpr(g, varNames, pathStack));
                            pathStack.removeLast();
                        }

                        case SequenceLayout s -> {
                            pathStack.addLast(groupElement(s.name().get()));
                            result.addAll(ofExpr(s, varNames, pathStack));
                            pathStack.removeLast();
                        }

                        default -> {}
                    }
                }
            }

            case SequenceLayout seq -> {
                pathStack.addLast(sequenceElement());
                result.addAll(ofExpr(seq.elementLayout(), varNames, pathStack));
                pathStack.removeLast();
            }

            default -> {}
        }

        return result;
    }

    
    private static Expr varHandleExpr(List<Expr> path) {
        return new Expr.CompositeExpr.Call(
            Expr.InvokeKind.INTERFACE,
            CD_MemoryLayout,
            "varHandle",
            MethodTypeDesc.of(CD_VarHandle, CD_PathElement.arrayType()),
            new Expr.BaseExpr.GetStatic("layout", CD_MemoryLayout),
            new Expr.CompositeExpr.ArrayLiteral(CD_PathElement, path)
        );
    }

    private static Expr groupElement(String name) {
        return new Expr.CompositeExpr.Call(
            Expr.InvokeKind.STATIC,
            CD_PathElement,
            "groupElement",
            MethodTypeDesc.of(CD_PathElement, CD_String),
            new Expr.BaseExpr.StringLiteral(name)
        );
    }

    private static Expr sequenceElement() {
        return new Expr.CompositeExpr.Call(
            Expr.InvokeKind.STATIC,
            CD_PathElement,
            "sequenceElement",
            MethodTypeDesc.of(CD_PathElement)
        );
    }

}

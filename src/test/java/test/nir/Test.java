/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

import java.util.List;
import static test.nir.Stmt.*;
import static test.nir.Expr.*;

/**
 *
 * @author jmburu
 */
public class Test {
    void main(){
        Stmt program =  new Block(List.of(
                            new StoreLocal(
                                1,
                                new NewIntArray(new IntConst(3))
                            ),
                            new ArrayStore(
                                new LoadLocal(1),
                                new IntConst(0),
                                new IntConst(4)
                            ),
                            new Return(
                                new LoadLocal(1)
                            )
                        ));
        program.emit(new DebugEmitter());
    }
}

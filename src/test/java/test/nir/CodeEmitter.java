/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

/**
 *
 * @author joemw
 */
public interface CodeEmitter {
    void iconst(int v);
    void newarrayInt();
    void aload(int slot);
    void astore(int slot);
    void iastore();
    void areturn();
}

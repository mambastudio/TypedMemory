/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test.nir;

/**
 *
 * @author joemw
 */
public class DebugEmitter implements CodeEmitter{

    @Override
    public void iconst(int value) {
        IO.println("iconst_" + value);
    }

    @Override
    public void newarrayInt() {
        IO.println("newarray int");
    }

    @Override
    public void aload(int slot) {
        IO.println("aload_" + slot);
    }

    @Override
    public void astore(int slot) {
        IO.println("astore_" + slot);
    }

    @Override
    public void iastore() {
        IO.println("iastore");
    }

    @Override
    public void areturn() {
        IO.println("areturn");
    }
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.mamba.typedmemory.api.Mem;
import java.lang.foreign.Arena;

/**
 *
 * @author joemw
 */
public class TestPipelines {
    void main(){
        test1();
    }
    
    public void test1(){
        record Student(int id, int score, boolean active){}
        
        try (Arena arena = Arena.ofConfined()) {
            var students = Mem.of(Student.class, arena, 10_000_000).init(()-> new Student(0, nextInt(0, 100), true));
            IO.println(students
                    .query()
                    .map(Student::score)
                    .filter(score -> score >= 50)
                    .count());     
        }        
    }
    
    //Random class has inefficient random functions, hence just use Math.random here
    private int nextInt(int min, int max){
        return (int) ((Math.random() * (max - min)) + min);
    }
}

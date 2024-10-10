package multithreading.Locks;

public class Counter {

     private int count=0;


     //u can make method or block of code synchronized
     public  void increment(){
         synchronized (this) {
             count++;
         }

     }

     public int getCount(){
         return  count;
     }
}

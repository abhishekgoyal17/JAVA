package multithreading;

public class World1 implements  Runnable{


    @Override
    public void run(){
        for(;;){
            System.out.println(Thread.currentThread().getName());
        }
    }
}

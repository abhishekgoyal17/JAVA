package multithreading.yield;

public class MyThread4 extends Thread{

   public MyThread4(String name){
       super(name);
   }
    @Override
    public void run() {

        for(int i=0;i<5;i++){
            System.out.println(Thread.currentThread().getName()+"running");
            Thread.yield();
        }
    }

    public static void main(String[] args) {

        MyThread4 t1=new MyThread4("t1");
        MyThread4 t2=new MyThread4("t2");
        t1.start();t2.start();
    }
}

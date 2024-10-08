package multithreading.interrupt;

public class MyThread3 extends Thread{



    @Override
    public void run() {

            try {
                Thread.sleep(200);
                System.out.println("running...");
            } catch (InterruptedException e) {
                System.out.println("Thread interuppted"+ e);
            }
        }


    public static void main(String[] args) {

        MyThread3 t1=new MyThread3();
        t1.start();
        t1.interrupt();



    }
}

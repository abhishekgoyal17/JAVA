package multithreading.lifecycle;

public class Mythread  extends  Thread {

    @Override
    public void run() {
        System.out.println("RUNNING");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Mythread t2=new Mythread();
        System.out.println(t2.getState());
        t2.start();
        System.out.println(t2.getState());
        System.out.println( Thread.currentThread().getState());
        Thread.sleep(100);
        System.out.println(t2.getState());
        t2.join();
        System.out.println(t2.getState());
    }
}

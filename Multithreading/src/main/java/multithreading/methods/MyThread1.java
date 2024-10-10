package multithreading.methods;



public class MyThread1 extends Thread {


    @Override
    public void run() {

        for(int i=1;i<=5;i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(i);
    }
    }

    public static void main(String[] args) throws InterruptedException {

        MyThread1 t1= new MyThread1();
        t1.start();
        //t1.join();
        System.out.println("Hello");

    }
}

//start run sleep join setPriority

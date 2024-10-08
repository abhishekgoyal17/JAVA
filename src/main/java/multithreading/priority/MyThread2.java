package multithreading.priority;

public class MyThread2 extends Thread{


  public MyThread2(String name){
      super(name);
  }
    @Override
    public void run() {

        for(int i=0;i<5;i++) {

            System.out.println(Thread.currentThread().getName()+"-Priority:"+Thread.currentThread().getPriority()+"-count:"+ i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

        MyThread2 l=new MyThread2("LOW PRIORITY");
        MyThread2 m=new MyThread2("LOW PRIORITY");
        MyThread2 n=new MyThread2("LOW PRIORITY");

        t2.setPriority(Thread.MIN_PRIORITY);
        t2.start();

    }
}

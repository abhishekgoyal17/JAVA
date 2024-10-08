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
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {

        MyThread2 l=new MyThread2("LOW PRIORITY");
        MyThread2 m=new MyThread2("MEDIUM PRIORITY");
        MyThread2 n=new MyThread2("HIGH PRIORITY");

        l.setPriority(Thread.MIN_PRIORITY);
        m.setPriority(Thread.NORM_PRIORITY);
        n.setPriority(Thread.MAX_PRIORITY);

        l.start();
        m.start();
        n.start();


    }
}

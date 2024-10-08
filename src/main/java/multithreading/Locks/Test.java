package multithreading.Locks;

public class Test {


    public static void main(String[] args) {
        Counter counter=new Counter();
        SyncThread t1= new SyncThread(counter);
        SyncThread t2= new SyncThread(counter);
        t1.start();
        t2.start();
        try{
            t1.join();
            t2.join();
        }catch (Exception e){

        }
        System.out.println(counter.getCount());

    }
}

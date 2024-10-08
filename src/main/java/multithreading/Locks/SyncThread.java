package multithreading.Locks;

public class SyncThread extends  Thread{
    @Override
    public void run() {
        for(int i=0;i<1000;i++){
            counter.increment();
        }
    }

    private Counter counter;
    public SyncThread (Counter counter){
        this.counter=counter;
    }

}

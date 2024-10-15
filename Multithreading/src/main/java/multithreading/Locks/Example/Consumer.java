package multithreading.Locks.Example;

public class Consumer implements  Runnable{

    SharedResource sharedResource;

    Consumer(SharedResource resource){
        this.sharedResource=resource;
    }

    @Override
    public void run(){
        System.out.println("Consumer thread"+Thread.currentThread().getName());
        sharedResource.consumeItem();
    }

}

package multithreading.Locks.Example;

public class Producer implements  Runnable{

    SharedResource sharedResource;

    Producer(SharedResource resource){
        this.sharedResource=resource;
    }

    @Override
    public void run(){
        System.out.println("Producer thread"+Thread.currentThread().getName());
        sharedResource.consumeItem();
    }

}

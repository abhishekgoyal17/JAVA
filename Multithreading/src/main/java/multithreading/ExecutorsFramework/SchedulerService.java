package multithreading.ExecutorsFramework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

    public static void main(String[] args) {

        ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(1);
        scheduler.schedule(
                ()-> System.out.println("Task executed after 5 second delay"),5, TimeUnit.SECONDS);
        scheduler.schedule(()->{
            System.out.println("Intiating shutdown");
            scheduler.shutdown();
        },20,TimeUnit.SECONDS);


    }
}

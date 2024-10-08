package multithreading.Locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {

    private int balance=100;
    private final Lock lock= new ReentrantLock();

    public void withdraw(int amount) throws InterruptedException {

        System.out.println(Thread.currentThread().getName()+"-> attempting to withdraw: "+amount);
        try{
            if(lock.tryLock(1000, TimeUnit.MILLISECONDS)){
                  if(balance>=amount){

                      try{
                          System.out.println(Thread.currentThread().getName()+"-> proceed to withdraw :"+balance);
                          Thread.sleep(3000);

                          balance-=amount;
                          System.out.println(Thread.currentThread().getName()+"-> completed to withdraw: "+balance);
                      }catch (InterruptedException e){
                          Thread.currentThread().interrupt();
                      }
                      finally {
                          lock.unlock();
                      }
                  }
                  else{
                      System.out.println(Thread.currentThread().getName()+" -> insufficent balance ");
                  }

            }
            else{
                System.out.println(Thread.currentThread().getName()+" ->could not acquire the lock,will try later");
            }

        }catch(Exception e){
           Thread.currentThread().interrupt();
        }







        //synchronized (implicit lock)
        /** if(balance>=amount){
            System.out.println(Thread.currentThread().getName()+"attempting to withdraw");
            try{
            Thread.sleep(10000);}catch (InterruptedException e){}

            balance-=amount;
            System.out.println(Thread.currentThread().getName()+"completed to withdraw"+balance);
        }
        else{
            System.out.println(Thread.currentThread().getName()+"insuffcient balance");
        }
         **/


    }
}

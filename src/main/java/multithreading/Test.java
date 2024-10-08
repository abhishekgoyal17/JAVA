package multithreading;



public class Test {
    public static void main(String[] args) {

        World world =new World();
        Thread t1=new Thread((world));
       // world.start();
        t1.start();
        System.out.println(Thread.currentThread().getName());
    }
}

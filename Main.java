import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        btree tree = new btree(3);

        long begin = System.currentTimeMillis();


        // retrieval

        // creating executor service with cached thread pool
        ExecutorService serv = Executors.newCachedThreadPool();
        for(int i = 0; i < 100; i++){
            
            int num = i;
            serv.submit(() -> {
                tree.retrieve(num);
            });
        }

        serv.shutdown();

        try{
            serv.awaitTermination(60, TimeUnit.SECONDS);
            serv.shutdownNow();
        }
        catch(InterruptedException e){
            serv.shutdownNow();
        }
        long end = System.currentTimeMillis();

        long time = end-begin;

        System.out.println("Elapsed Time: " + time + "milliseconds");
     }
}

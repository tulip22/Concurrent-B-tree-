import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        BPlusTree tree = new BPlusTree(3);


        for(int i = 0; i < 100; i++){
            tree.insert(new StudentRecord(i,"AA"));
        }
        // Declaring the ArrayList
        List<StudentRecord> arr = Collections.synchronizedList(new ArrayList<>(Collections.nCopies(100, null)));
        long begin = System.currentTimeMillis();

        // retrieval
        // creating executor service with cached thread pool
        ExecutorService serv = Executors.newCachedThreadPool();
        for(int i = 0; i < 200; i++){
            
            int num = i;
            serv.submit(() -> {
                //writing a function to fetch n store , then print in sorted order
                //create arrayList and pass it in fetch , after all threads are done, 
                StudentRecord s = tree.retrieve(num);
                synchronized(arr){
                    if(s!=null){
                        //System.out.println(s.rollNo);
                        arr.add(s.rollNo, s);
                        System.out.println((arr.get(s.rollNo)).toString());
                    }
                    else{
                        arr.add(num, null);
                    }
                }
                
            });
        }

        serv.shutdown();

        try{
            serv.awaitTermination(300, TimeUnit.SECONDS);
            serv.shutdownNow();
        }
        catch(InterruptedException e){
            serv.shutdownNow();
        }

        //printing records in sorted order
        arr.forEach((s) ->{
            if(s!= null){
                System.out.println(s.toString());
            }
            else{
                System.out.println("Roll no: " + s.rollNo + "Name: NA");
            }
           
        }); 

        long end = System.currentTimeMillis();

        long time = end-begin;

        System.out.println("Elapsed Time: " + time + "milliseconds");
     }
}

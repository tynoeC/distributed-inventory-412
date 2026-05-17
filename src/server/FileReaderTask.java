package server;
import java.io.*;
import java.util.concurrent.*;

//reads inventory data from a file
public class FileReaderTask implements Runnable {
    private String filename; //name of the inventory file to read
    private ConcurrentHashMap<String, Double> mergedMap; //shared thread safe map to store merged product data

    public FileReaderTask(String filename, ConcurrentHashMap<String, Double> mergedMap){
        this.filename = filename;
        this.mergedMap = mergedMap;
    }
    //Thread excution method
    public void run(){
        try(BufferedReader reader = new BufferedReader(new FileReader("src/inventory/" + filename))) {
            String line;
            //reading file line by line
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");//split line using a comma
                String productID = parts[0].trim();
                double price = Double.parseDouble(parts[2].trim());
                
                //storing product and highest price in shared map
                //merge() updates values in concurrent environment
                mergedMap.merge(productID, price, Math::max);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

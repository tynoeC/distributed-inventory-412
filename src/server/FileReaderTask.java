package server;
import java.io.*;
import java.util.concurrent.*;

public class FileReaderTask implements Runnable {
    private String filename;
    private ConcurrentHashMap<String, Double> mergedMap;

    public FileReaderTask(String filename, ConcurrentHashMap<String, Double> mergedMap){
        this.filename = filename;
        this.mergedMap = mergedMap;
    }
    public void run(){
        try(BufferedReader reader = new BufferedReader(new FileReader("src/inventory/" + filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String productID = parts[0].trim();
                double price = Double.parseDouble(parts[2].trim());
                
                mergedMap.merge(productID, price, Math::max);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

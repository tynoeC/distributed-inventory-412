package server;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

public class ClientHandler implements Runnable{
    private Socket clientSocket;

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
    }
    public void run(){
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)){
            File inventoryDir = new File("src/inventory/");
        
            // Geting all files in the directory
            File[] files = inventoryDir.listFiles();
        
            // Build the comma-separated string dynamically
            StringBuilder fileListBuilder = new StringBuilder();
        
            if (files != null) {
                for (File file : files) {
                // Only include actual files (skipping subdirectories if any exist)
                    if (file.isFile()) {
                        if (fileListBuilder.length() > 0) {
                            fileListBuilder.append(",");
                        }
                    fileListBuilder.append(file.getName());
                }
            }
        }
        out.println(fileListBuilder.toString());

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String clientCommand;
        while ((clientCommand = in.readLine()) != null) {
            String command = clientCommand.trim();
            if(command.startsWith("GET ")){
                String filename = command.substring(4);
                File file = new File("src/inventory/" + filename);
                try(BufferedReader fileReader = new BufferedReader(new FileReader(file))){
                    String line; 
                    while((line = fileReader.readLine()) != null)
                        out.println(line);
                    out.println("END");
                }
                catch (IOException e) {
                    out.println("Unable to read file");
                }
            } 
            else if ("OVERVIEW".equalsIgnoreCase(command)){
                ConcurrentHashMap<String, Double> mergedMap = new ConcurrentHashMap<>();
                ExecutorService executor = Executors.newFixedThreadPool(4);
                String[] inventoryFiles = {"Electronics.txt", "Groceries.txt", "Books.txt", "Clothing.txt"};

                for(String filename : inventoryFiles){
                    executor.submit(new FileReaderTask(filename, mergedMap));
                }
                
                executor.shutdown();

                try {
                    if(executor.awaitTermination(60, TimeUnit.SECONDS)){
                        System.out.println("All threads finished. Map size: " + mergedMap.size());
                        if(mergedMap.isEmpty()){
                            out.println("No product found!!!");
                        } else{
                            double sum = 0;
                            double highest = Double.MIN_VALUE;
                            double lowest = Double.MAX_VALUE;
                            String highestName = "";
                            String lowestName = "";

                            for(Map.Entry<String, Double> entry : mergedMap.entrySet()){
                                String productID = entry.getKey();
                                double price = entry.getValue();
                                sum += price;
                                if(price > highest){
                                    highest = price;
                                    highestName = productID;
                                }
                                if (price < lowest){
                                    lowest = price;
                                    lowestName = productID;
                                }
                            }
                            double average = sum / mergedMap.size();

                            out.println("Total product: " + mergedMap.size());
                            out.println("Average Price: " + String.format("%.2f", average));
                            out.println("Highest Price: " + highestName + " ($" + String.format("%.2f", highest) + ")");
                            out.println("Lowest Price: " + lowestName + " ($" + String.format("%.2f", lowest) + ")");

                            writeMergedFile(mergedMap);
                    

                            out.println("END");
                        }
                    }
                    else{
                        System.err.println("Thread pool timed out before finishing");
                        out.println("Error: Server timeout");
                    }
                } 
                catch(InterruptedException e){
                    System.err.println("Thread pool timed out before finishing" + e.getMessage());
                    out.println("Error: Server processing interrupted");
                    Thread.currentThread().interrupt();
                }
            }
            else if ("VERIFY".equalsIgnoreCase(command)){
                int digitSum = 26; //sum of 22202963
                int port = clientSocket.getLocalPort();
                int hash = (digitSum * port) % 1000;

                out.println(hash);
            }
            else if ("BYE".equalsIgnoreCase(command)){
                System.out.println("Handling BYE command");
                break;
            }
            else {
                out.println("Error; Unknown command: " + command);
            }
            
        }
           
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    private void writeMergedFile(ConcurrentHashMap<String, Double> mergedMap){
        File targetFile = new File("src/inventory/", "MergedInventory.txt");
        try(PrintWriter fileOut = new PrintWriter(new FileWriter(targetFile))){
            for (Map.Entry<String, Double> entry : mergedMap.entrySet()){
                String productID = entry.getKey();
                double price = entry.getValue();

                fileOut.println(productID + ", " + String.format("%.2f", price));

            }
            System.out.println("Successfully generated MergedInventory.txt on server");

        }catch(IOException e){
            System.err.println("Failed to write merged inventory file: " + e.getMessage());
        }
    }
}

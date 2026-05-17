package server;
import java.io.IOException;
import java.net.*;

public class InventoryServer {
    public static void main(String[] args){
        int port = 5963; //port number generated from adding 5000 & 2963%1000

        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server started at port: " + port);
            System.out.println("Waiting for client connection......");
            //server waits for clients to connect by communication with the ClientHandler.java file to allow more than one client to connect 
            while(true){
                try{
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    new Thread(handler).start();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                }
                catch(IOException e){
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
}

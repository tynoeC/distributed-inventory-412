package server;
import java.io.IOException;
import java.net.*;

public class InventoryServer {
    public static void main(String[] args){
        int port = 5963;

        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server started at port: " + port);
            System.out.println("Waiting for client connection......");
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

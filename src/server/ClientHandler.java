package server;
import java.net.*;

public class ClientHandler implements Runnable{
    private Socket clientSocket;

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
    }
    public void run(){
        
    }
}

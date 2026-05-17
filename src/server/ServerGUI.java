package server;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;


public class ServerGUI extends JFrame{
    private ServerSocket serverSocket;//server socket to listen for client connections

    private JTextField IPField;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;

    private JTextArea logArea;// display server logs
    private JButton clearLogButton;// clears the logs

    public ServerGUI(){
        setTitle("Inventory server");
        setSize(850,650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        //connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,5));
        JLabel IPLabel = new JLabel("Server IP Address: ");
        
        try{//automatically display local ip address
            IPField = new JTextField(InetAddress.getLocalHost().getHostAddress(), 10);
            IPField.setEditable(false);//prevents editing the ip field
        }catch(UnknownHostException e){
            IPField = new JTextField("localhost", 10);
            e.printStackTrace();
        }
        JLabel portLabel = new JLabel("Port: ");
        portField = new JTextField("5963", 5); //default server port

        //server control buttons
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        statusLabel = new JLabel("Stopped");

        //adiing components to connection panel
        connectionPanel.add(IPLabel);
        connectionPanel.add(IPField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);
        connectionPanel.add(startButton);
        connectionPanel.add(stopButton);
        connectionPanel.add(statusLabel);
        add(connectionPanel, BorderLayout.NORTH);

        //center panel
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        logArea = new JTextArea();//log area for server messages
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        centerPanel.add(logScrollPane, BorderLayout.CENTER);

        //log action panel
        JPanel logActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        clearLogButton = new JButton("Clear Log");
        logActionPanel.add(clearLogButton);
        centerPanel.add(logActionPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        //clear log button 
        clearLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                logArea.setText("");//clear server logs
            }
        });

        //start button
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                //read port entered by user
                String portStr = portField.getText().trim();
                //create separate server for server
                Thread serverThread = new Thread(new Runnable() {
                    public void run(){
                        logArea.append("Server started by Student Tinotenda (ID:22202963)\n");
                        logArea.append("Server started at port: " + portStr + "\n");

                        try {
                            int port = Integer.parseInt(portStr);
                            serverSocket = new ServerSocket(port);

                            //keep accepting client connections
                            while(!serverSocket.isClosed()){
                                try {
                                    Socket clienSocket = serverSocket.accept();//waits for client connection
                                    String clientIP = clienSocket.getInetAddress().getHostAddress();//get ip address

                                    logArea.append("Client connected from: " + clientIP + "\n");
                                    //create client handler thread
                                    ClientHandler handler = new ClientHandler(clienSocket);
                                    new Thread(handler).start();
                                } catch (IOException ex) {
                                    if(serverSocket.isClosed())
                                        break;

                                    logArea.append("Error accepting client connection: " + ex.getMessage() + "\n");
                                }
                            }
                        }
                        catch(NumberFormatException e){
                            logArea.append("Startup Error: Invalid port format \n");
                            resetToStoppedState();
                        } 
                        catch (IOException e) {
                            logArea.append("Startup Error: Could not listen to port" + portStr + "\n");
                            resetToStoppedState();
                        }

                    }
                });
                serverThread.start();// start server thread
                //update GUI state
                statusLabel.setText("Running");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        //stop button
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{//close server socket if active
                    if (serverSocket != null && !serverSocket.isClosed()){
                        serverSocket.close();
                    }
                    logArea.append("Server stopped\n");
                        
                }
                catch (IOException ex){
                    logArea.append("Error occured while shutting down: " + ex.getMessage() + "\n");
                }
                finally{
                    resetToStoppedState();
                }
            }
        });
    }

    //resets GUI controls when server stops
    private void resetToStoppedState(){
            SwingUtilities.invokeLater(new Runnable() {
                public void run(){
                    statusLabel.setText("Stopped");
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                }
            });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            ServerGUI server = new ServerGUI();
            server.setVisible(true);
        });
    }

}
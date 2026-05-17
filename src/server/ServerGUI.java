package server;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;


public class ServerGUI extends JFrame{
    private ServerSocket serverSocket;

    private JTextField IPField;
    private JTextField portField;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;

    private JTextArea logArea;
    private JButton clearLogButton;

    public ServerGUI(){
        setTitle("Inventory server");
        setSize(850,650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,5));
        JLabel IPLabel = new JLabel("Server IP Address: ");
        
        try{
            IPField = new JTextField(InetAddress.getLocalHost().getHostAddress(), 10);
            IPField.setEditable(false);
        }catch(UnknownHostException e){
            IPField = new JTextField("localhost", 10);
            e.printStackTrace();
        }
        JLabel portLabel = new JLabel("Port: ");
        portField = new JTextField("5963", 5);

        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);

        statusLabel = new JLabel("Stopped");

        connectionPanel.add(IPLabel);
        connectionPanel.add(IPField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);
        connectionPanel.add(startButton);
        connectionPanel.add(stopButton);
        connectionPanel.add(statusLabel);
        add(connectionPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        centerPanel.add(logScrollPane, BorderLayout.CENTER);

        JPanel logActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        clearLogButton = new JButton("Clear Log");
        logActionPanel.add(clearLogButton);
        centerPanel.add(logActionPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        clearLogButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                logArea.setText("");
            }
        });

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String portStr = portField.getText().trim();
                Thread serverThread = new Thread(new Runnable() {
                    public void run(){
                        logArea.append("Server started by Student Tinotenda (ID:22202963)\n");
                        logArea.append("Server started at port: " + portStr + "\n");

                        try {
                            int port = Integer.parseInt(portStr);
                            serverSocket = new ServerSocket(port);

                            while(!serverSocket.isClosed()){
                                try {
                                    Socket clienSocket = serverSocket.accept();
                                    String clientIP = clienSocket.getInetAddress().getHostAddress();

                                    logArea.append("Client connected from: " + clientIP + "\n");
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
                serverThread.start();
                statusLabel.setText("Running");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{
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
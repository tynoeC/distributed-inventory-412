package client;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InventoryClient extends JFrame{
    private JTextField IPField;
    private JTextField portField;
    private JButton connectionButton;
    private JButton disconnectButton;
    private JLabel statusLabel;

    private JPanel fileButtonPanel;
    private DefaultTableModel tableModel;
    private JTable productTable;

    private JButton overviewButton;
    private JButton verifyButton;
    private JTextArea outputArea;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public InventoryClient(){
        setTitle("Inventory client");
        setSize(850,650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,5));
        JLabel IPLabel = new JLabel("IP Address: ");
        IPField = new JTextField("localhost", 10);

        JLabel portLabel = new JLabel("Port: ");
        portField = new JTextField("5963", 5);

        connectionButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);

        statusLabel = new JLabel("Disconnected");

        connectionPanel.add(IPLabel);
        connectionPanel.add(IPField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);
        connectionPanel.add(connectionButton);
        connectionPanel.add(disconnectButton);
        connectionPanel.add(statusLabel);
        add(connectionPanel, BorderLayout.NORTH);

        fileButtonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JScrollPane leftScrollPane = new JScrollPane(fileButtonPanel);

        String[] columnNames = {"Product ID", "Name", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0);
        productTable = new JTable(tableModel);
        JScrollPane rightScrollPane = new JScrollPane(productTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        overviewButton = new JButton("Get Overview");
        verifyButton = new JButton("Verify");

        overviewButton.setEnabled(false);
        verifyButton.setEnabled(false);

        actionButtonPanel.add(overviewButton);
        actionButtonPanel.add(verifyButton);
        southPanel.add(actionButtonPanel, BorderLayout.NORTH);

        outputArea = new JTextArea(8, 50);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        southPanel.add(outputScrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        connectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String ip = IPField.getText().trim();
                String portStr = portField.getText().trim();

               try {
                int port = Integer.parseInt(portStr);
                socket = new Socket(ip, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                outputArea.append("Connection to server successfully!!!!\n");

                String fileListLine = in.readLine();
                if (fileListLine != null && !fileListLine.trim().isEmpty()){
                    String[] files = fileListLine.split(",");
                    fileButtonPanel.removeAll();

                    for(String fileName : files){
                        String cleanFileName = fileName.trim();
                        JButton fileButton = new JButton(cleanFileName);

                        fileButton.addActionListener(ev -> {
                            try {
                                tableModel.setRowCount(0);
                                out.println("GET " + cleanFileName);

                                String line;
                                while(!(line = in.readLine()).equals("END")){
                                    String[] parts = line.split(",");
                                    if (parts.length >= 3){
                                        tableModel.addRow(new Object[]{
                                            parts[0].trim(),
                                            parts[1].trim(),
                                            parts[2].trim()
                                        });
                                    }
                                }
                                outputArea.append("Loaded file: " + fileName + "\n");

                            } catch (IOException ex2) {
                                outputArea.append("Erroe loading file\n");
                            }
                        });
                        fileButtonPanel.add(fileButton);
                    }
                    fileButtonPanel.revalidate();
                    fileButtonPanel.repaint();
                }
                statusLabel.setText("Connected");
                connectionButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                overviewButton.setEnabled(true);
                verifyButton.setEnabled(true);

               } 
               catch(NumberFormatException ex){
                outputArea.append("Connecion Error: Invalid port format.\n");

               }
               catch (IOException ex) {
                outputArea.append("Connection failed: " + ex.getMessage());
               }
            }
        });

        overviewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{
                    outputArea.setText("");
                    out.println("OVERVIEW");
                    String responseLine;
                    while((responseLine = in.readLine()) != null){
                        if ("END".equals((responseLine.trim()))){
                            break;
                        }
                        outputArea.append(responseLine + "\n");
                    }
                } 
                catch(IOException ex){
                    outputArea.append("Error retriving inventory overview: " + ex.getMessage() + "\n");
                }
            }
        });

        verifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{
                    outputArea.setText("");
                    out.println("VERIFY");
                    String hasResponse = in.readLine();
                    if(hasResponse != null){
                        outputArea.append("Server verification has: " + hasResponse.trim() + "\n");
                    }else{
                        outputArea.append("Verification failed.\n");
                    }
                } 
                catch(IOException ex){
                    outputArea.append("Error during verification request: " + ex.getMessage() + "\n");
                }
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{
                    if (out != null)
                        out.println("BYE");

                    outputArea.append("Disconnecting from server.....\n");
                }finally{
                    try{
                        if(in != null) in.close();
                        if(out != null) out.close();
                        if(socket != null && !socket.isClosed()) socket.close();
                    } catch(IOException ex){
                        System.err.println("Error closing connection resources: " + ex.getMessage());

                    }
                    fileButtonPanel.removeAll();
                    fileButtonPanel.revalidate();
                    fileButtonPanel.repaint();

                    tableModel.setRowCount(0);

                    statusLabel.setText("Disconnected");
                    connectionButton.setEnabled(true);
                    disconnectButton.setEnabled(false);
                    overviewButton.setEnabled(false);
                    verifyButton.setEnabled(false);

                    outputArea.append("Successfully disconnected.\n");
                }
            }
        });

    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            InventoryClient client = new InventoryClient();
            client.setVisible(true);
        });
    }
}

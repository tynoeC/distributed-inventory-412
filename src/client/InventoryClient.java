package client;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

//Client GUI for interacting with the inventory server
public class InventoryClient extends JFrame{
    private JTextField IPField;
    private JTextField portField;
    private JButton connectionButton;
    private JButton disconnectButton;
    private JLabel statusLabel; // diplay the connection status

    private JPanel fileButtonPanel;//panel containing inventory file buttons
    private DefaultTableModel tableModel;// table model to display product
    private JTable productTable;

    private JButton overviewButton;
    private JButton verifyButton;
    private JTextArea outputArea;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    //Constructor to build the GUI
    public InventoryClient(){
        setTitle("Inventory client");
        setSize(850,650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,5));
        JLabel IPLabel = new JLabel("IP Address: ");
        IPField = new JTextField("localhost", 10); //default server IP

        JLabel portLabel = new JLabel("Port: ");// default server port
        portField = new JTextField("5963", 5);

        connectionButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false); //disconnect butto disabled initially

        statusLabel = new JLabel("Disconnected");

        //adding components to top panel
        connectionPanel.add(IPLabel);
        connectionPanel.add(IPField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portField);
        connectionPanel.add(connectionButton);
        connectionPanel.add(disconnectButton);
        connectionPanel.add(statusLabel);
        add(connectionPanel, BorderLayout.NORTH);

        //Left panel displaying file buttons
        fileButtonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JScrollPane leftScrollPane = new JScrollPane(fileButtonPanel);

        //right panel showing the table columns
        String[] columnNames = {"Product ID", "Name", "Price"};
        tableModel = new DefaultTableModel(columnNames, 0); //stores product data
        productTable = new JTable(tableModel);
        JScrollPane rightScrollPane = new JScrollPane(productTable);

        //split screen btwn file buttons and product table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerLocation(250);

        add(splitPane, BorderLayout.CENTER);

        //south panel
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        //action buttons disabled intil connected
        overviewButton = new JButton("Get Overview");
        verifyButton = new JButton("Verify");

        overviewButton.setEnabled(false);
        verifyButton.setEnabled(false);

        actionButtonPanel.add(overviewButton);
        actionButtonPanel.add(verifyButton);
        southPanel.add(actionButtonPanel, BorderLayout.NORTH);

        //Output are for logs and server messages
        outputArea = new JTextArea(8, 50);
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        southPanel.add(outputScrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        //connect button 
        connectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String ip = IPField.getText().trim();//read ip and port entered by the user 
                String portStr = portField.getText().trim();

               try {
                int port = Integer.parseInt(portStr);
                socket = new Socket(ip, port);//socket connection to server
                out = new PrintWriter(socket.getOutputStream(), true);//output stream
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));//input stream
                
                outputArea.append("Connection to server successfully!!!!\n");

                String fileListLine = in.readLine();// reads available inventory files and if they exists
                if (fileListLine != null && !fileListLine.trim().isEmpty()){
                    String[] files = fileListLine.split(",");
                    fileButtonPanel.removeAll();

                    //creating a button for each file 
                    for(String fileName : files){
                        String cleanFileName = fileName.trim();
                        JButton fileButton = new JButton(cleanFileName);

                        //file button action
                        fileButton.addActionListener(ev -> {
                            try {
                                tableModel.setRowCount(0); //clear old table data
                                out.println("GET " + cleanFileName); //request file from server

                                String line;
                                //Read file contents
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
                        //Adding button to the panel
                        fileButtonPanel.add(fileButton);
                    }
                    //refreshing the panel
                    fileButtonPanel.revalidate();
                    fileButtonPanel.repaint();
                }
                //Updating GUI state after successful connection
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

        //overview button, getting overvie from the server, reading overvie data and stop when end is received 
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
                        //display the response
                        outputArea.append(responseLine + "\n");
                    }
                } 
                catch(IOException ex){
                    outputArea.append("Error retriving inventory overview: " + ex.getMessage() + "\n");
                }
            }
        });

        // verify button sends verification request and hash
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

        //disconnect button 
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{//notify server before disconnecting 
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
                    //remove file buttons
                    fileButtonPanel.removeAll();
                    fileButtonPanel.revalidate();
                    fileButtonPanel.repaint();

                    tableModel.setRowCount(0); //clears the table

                    //rest GUI state
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
    //main method to start the GUI applicaton
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->{
            InventoryClient client = new InventoryClient();
            client.setVisible(true);
        });
    }
}

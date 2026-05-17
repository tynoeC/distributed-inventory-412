# Project CMPE412: System Programming

## Distributed Inventory Management in Java

## Overview
The client-server system manages product inventory data. The server hosts multiple
inventory files (one per category). Clients connect from a different PC to view product lists
or request statistical overviews. 

## Concept Used
- Client-Server Architecture
- Socket Programming
- Multi-Threading
- Concurrency
- Thread Pools(ExecutorService)
- Runnable Interface
- Thread-Safe Data Structures
- File I/O
- GUI Programing with Swing
- Event-Driven Programming
- Network Communication Protocol Design
- Data Processing and Analysis
- Exception Handling
- Synchronization and Safe Resource Sharing
- GUI State Managemanent

## Description 
This project is an inventory system that allows many clients to connect to the server at the
same time and work with product data without crashing the program or damaging the files.
The server handles client connections and file reading using ClientHandler.java and
FileReaderTask.java, while the client side uses InventoryClient.java to let users view
products and analytics. The system uses ServerSocket for communication between the
server and clients, thread pools to run tasks at the same time, and ConcurrentHashMap to
safely manage shared data between threads. The port number is student ID based and is
then used with ID student sum to produce the verification code, and the student Id was
used in seeding the inventory content.

## System Components

### Server Side
The server side is responsible for:
- Listening for incoming client connections
- Handling multiple clients concurrently
- Reading inventory files
- Generating inventory statistics
- Managing shared inventory data safely

#### Main Classes
- `ServerGUI.java`
- `ClientHandler.java`
- `FileReaderTask.java`
-  `InventoryServer.java`

---

### Client Side
The client side provides a graphical interface that allows users to:
- Connect to the server
- View inventory files
- Display products in tables
- Request inventory overview statistics
- Verify server responses

#### Main Class
- `InventoryClient.java`


## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

```text
src/
|
├── client/
│   └── InventoryClient.java
│
├── server/
│   ├── ServerGUI.java
│   ├── ClientHandler.java
│   └── FileReaderTask.java
│
└── inventory/
    ├── Electronics.txt
    ├── Groceries.txt
    ├── Books.txt
    ├── Clothing.txt
    └── MergedInventory.txt
```
## How to Run the Project

### 1. Start the Server
Run:

```bash
ServerGUI.java
```

Then click the **Start** button.

---

### 2. Start the Client
Run:

```bash
InventoryClient.java
```

Connect using:
- Server IP Address
- Server Port
---
## Demo Checklist
- [ ] Server running on port 5963
- [ ] Client connects from different PC
- [ ] File list appears dynamically
- [ ] Products display in table
- [ ] Overview stats shown
- [ ] Verify button shows hash 038
- [ ] P9999 record visible in table

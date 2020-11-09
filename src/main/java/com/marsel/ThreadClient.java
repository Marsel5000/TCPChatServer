package com.marsel;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadClient implements Runnable {
    private final Socket client;
    private String name;
    private DataOutputStream outputStream;

    ThreadClient(Socket socket) {
        client = socket;
    }

    synchronized public void write(String message) {
        try {
            outputStream.writeUTF(message);
            outputStream.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    boolean getName() {
        if (ServerFormUI.getStrings().contains(name)) return false;
        ServerFormUI.addStrings(name);
        return true;
    }

    @Override
    public void run() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            DataInputStream inputStream = new DataInputStream(client.getInputStream());
            outputStream = new DataOutputStream(client.getOutputStream());
            outputStream.flush();
            while (!client.isClosed()) {
                String entry = inputStream.readUTF();
                if (entry.contains("<name>") && entry.contains("<end>")) {
                    entry = entry.replace("<name>", "");
                    entry = entry.replace("<end>", "");
                    name = entry;
                    if (getName()) {
                        entry = "<name>ACCESS GRANTED\n<end>";
                        outputStream.writeUTF(ServerFormUI.getHistoryChat().toString());
                        outputStream.flush();
                        for (ThreadClient threadClient : ServerFormUI.getSockets()) {
                            threadClient.write("The client under the nickname " + name + " has joined the chat\n");
                        }
                        ServerFormUI.addSockets(this);
                    } else {
                        entry = "<name>ACCESS DENIED\n<end>";
                        name = "Entry other name";
                    }
                    outputStream.writeUTF(name + " " + entry);
                    outputStream.flush();
                    continue;
                }
                if (entry.contains("<start>") && entry.contains("<end>")) {
                    entry = entry.replace("<start>", "");
                    entry = entry.replace("<end>", "");
                }
                entry = formatter.format(date) + " " + name + ": " + entry + "\n";
                ServerFormUI.appendHistoryChat(entry);
                for (ThreadClient threadClient : ServerFormUI.getSockets()) {
                    threadClient.write(entry);
                }
            }
            inputStream.close();
            outputStream.close();
            client.close();
        } catch (IOException exception) {
            ServerFormUI.removeClient(this);
            ServerFormUI.removeName(name);
            for (ThreadClient threadClient : ServerFormUI.getSockets()) {
                threadClient.write("The client under the nickname " + name + " left the chat\n");
            }
        }
    }
}

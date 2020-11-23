package com.marsel;
import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadClient implements Runnable {

    ///JSON type
    private final String JSON_NAME = "<name>";
    private final String JSON_START = "<start>";
    private final String JSON_END = "<end>";

    private Socket client;
    private String name;
    private DataOutputStream outputStream;
    private String entry = "";

    ThreadClient(Socket socket) {
        client = socket;
    }

    synchronized public void sendToClient(String message) {
        try {
            outputStream.writeUTF(message);
            outputStream.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

    private boolean getName() {
        if (Server.getStrings().contains(name)) return false;
        Server.addStrings(name);
        return true;
    }

    private void clearJsonType(String type) {
        entry = entry.replaceFirst(type, "");
        StringBuilder b = new StringBuilder(entry);
        b.replace(entry.lastIndexOf(JSON_END), entry.lastIndexOf(JSON_END) + 5, "" );
        entry = b.toString();
    }

    private boolean parseMessage() throws IOException {
        DateFormat currentDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if (entry.startsWith(JSON_NAME) && entry.endsWith(JSON_END)) {
            clearJsonType(JSON_NAME);
            name = entry;
            if (getName()) {
                entry = "ACCESS GRANTED\n";
                outputStream.writeUTF(Server.getHistoryChat().toString());
                outputStream.flush();
                for (ThreadClient threadClient : Server.getSockets()) {
                    threadClient.sendToClient("The client under the nickname " + name + " has joined the chat\n");
                }
                Server.addSockets(this);
            } else {
                entry = "ACCESS DENIED\n";
                name = "Entry other name";
            }
            outputStream.writeUTF(JSON_NAME + name + " " + entry + JSON_END);
            outputStream.flush();
            return true;
        }
        if (entry.startsWith(JSON_START) && entry.endsWith(JSON_END)) clearJsonType(JSON_START);
        entry = currentDateTime.format(new Date()) + " " + name + ": " + entry + "\n";
        return false;
    }

    @Override
    public void run() {
        try ( DataInputStream inputStream = new DataInputStream(client.getInputStream())){
            outputStream = new DataOutputStream(client.getOutputStream());
            outputStream.flush();
            while (!client.isClosed()) {
                entry = inputStream.readUTF();
                if (parseMessage()) continue;
                Server.appendHistoryChat(entry);
                for (ThreadClient threadClient : Server.getSockets()) {
                    threadClient.sendToClient(entry);
                }
            }
            outputStream.close();
            client.close();
        } catch (IOException exception) {
            Server.removeClient(this);
            Server.removeName(name);
            for (ThreadClient threadClient : Server.getSockets()) {
                threadClient.sendToClient("The client under the nickname " + name + " left the chat\n");
            }
        }
    }
}

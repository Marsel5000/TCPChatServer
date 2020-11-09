package com.marsel;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.*;

public final class Server {
    private static final LinkedList<String> names = new LinkedList<>();
    private static final LinkedList<ThreadClient> sockets = new LinkedList<>();
    private static final StringBuilder historyChat = new StringBuilder( );

    synchronized public static StringBuilder getHistoryChat( ) {
        return historyChat;
    }
    synchronized public static void appendHistoryChat(String message) throws IOException {
        message = message.replace("<start>", "");
        message = message.replace("<name>", "");
        message = message.replace("<end>", "");
        historyChat.append(message);
        FileWriter fileWriter = new FileWriter("Test.txt", true);
        fileWriter.write( message );
        fileWriter.flush();
        fileWriter.close();
    }
    synchronized public static LinkedList<ThreadClient> getSockets( ) {
        return sockets;
    }
    synchronized public static void addSockets(ThreadClient client){
        sockets.add(client);
    }
    synchronized public static void removeClient(ThreadClient client ){
        sockets.remove(client);
    }
    synchronized public static LinkedList<String> getStrings(){
        return names;
    }
    synchronized public static void addStrings( String name ){
        names.add( name );
    }
    synchronized public static void removeName(String name ){
        names.remove(name);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LinkedList<ThreadClient> sockets = new LinkedList<>();
                File file = new File( "Test.txt" );
                try (ServerSocket server = new ServerSocket( 80 )){
                    file.createNewFile();
                    BufferedReader bufferedReader = new BufferedReader(new FileReader("Test.txt" ));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        historyChat.append(line).append("\n");
                    }
                    bufferedReader.close();

                    ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 60,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100));
                    while (true) {
                        Socket client = null;
                        client = server.accept();
                        ThreadClient threadClient = new ThreadClient( client );
                        executor.execute(threadClient);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}


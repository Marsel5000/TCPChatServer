package com.marsel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public final class Server {
    private static final int PORT = 80;
    private static final String FILE_NAME = "HistoryOfChat.txt";
    private static List<String> names = new LinkedList<>();
    private static List<ThreadClient> sockets = new LinkedList<>();
    private static StringBuilder historyOfChat = new StringBuilder( );

    synchronized public static StringBuilder getHistoryChat( ) {
        return historyOfChat;
    }

    synchronized public static void appendHistoryChat(String message) {
        historyOfChat.append(message);
        try (FileWriter fileWriter = new FileWriter(FILE_NAME, true)){
            fileWriter.write( message );
            fileWriter.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    synchronized public static List<ThreadClient> getSockets() {
        return sockets;
    }

    synchronized public static void addSockets(ThreadClient client){
        sockets.add(client);
    }

    synchronized public static void removeClient(ThreadClient client ){
        sockets.remove(client);
    }

    synchronized public static List<String> getStrings(){
        return names;
    }

    synchronized public static void addStrings(String name){
        names.add( name );
    }

    synchronized public static void removeName(String name){
        names.remove(name);
    }

    public static void main(String[] args){
        File file = new File( FILE_NAME );
        try (ServerSocket server = new ServerSocket( PORT );
             BufferedReader bufferedReader = new BufferedReader(new FileReader( FILE_NAME ))){
            file.createNewFile();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                historyOfChat.append(line).append("\n");
            }
            ThreadPoolExecutor executor = new ThreadPoolExecutor(100, 100, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
             while (true) {
                ThreadClient threadClient = new ThreadClient(server.accept());
                executor.execute(threadClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


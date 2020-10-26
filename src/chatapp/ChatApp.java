/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapp;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author johnvu
 */
public class ChatApp implements Runnable{
    
    ServerSocket server;
    Socket client = null;
    int port;
    int clientcount=0;
    BufferedReader in1, in2; 
    PrintWriter out1;
    String[] s;
    String c, in, out;
    Scanner cc=new Scanner(System.in);
    Thread t1, t2;
    boolean t;
    ExecutorService pool = null;
    Map<Integer,String[]> list = new HashMap<>();
    Map<Integer,Socket> listSocket = new HashMap<>();
    int id = 1;
    
    ChatApp(int port){
        this.port=port;
        this.t = true;
        
        try {
            t1 = new Thread(this);
            t2 = new Thread(this);
            
            server=new ServerSocket(port);
            pool = Executors.newFixedThreadPool(5);
            System.out.println("Server Ready, listen on port: " + port);
            t1.start();
            t2.start();
        } catch (IOException e) {
        }
    }
    
    @Override
    public void run() {
        try {
            if (Thread.currentThread() == t1) {
                //client = new Socket("localhost", server.getLocalPort());
                //READ USER INPUT
                do{
                    in1 = new BufferedReader(new InputStreamReader(System.in));
                    c = in1.readLine();
                    s=c.split(" ",3);
                            if(s[0].equalsIgnoreCase("help")){
                                System.out.println(" myip display IP address"
                                        + "\n myport distplay port"
                                        + "\n connect connect to another peer"
                                        + "\n list show all connected"
                                        + "\n send send messages to peers"
                                        + "\n terminate terminate  the  connection"
                                        + "\n exit exit the program");
                            } else if (s[0].equalsIgnoreCase("myip")){
                                System.out.println(" Your IP address is "+ InetAddress.getLocalHost().getHostAddress());
                            } else if (s[0].equalsIgnoreCase("myport")){
                                System.out.println(" Your port number is "+ server.getLocalPort());
                            } else if (s[0].equalsIgnoreCase("connect")){
                                try{
                                    client = new Socket(s[1], Integer.parseInt(s[2]));
                                    PrintWriter pr1 = new PrintWriter(client.getOutputStream(), true);
                                    pr1.println(InetAddress.getLocalHost().getHostAddress() + " " + server.getLocalPort());
                                    t=false;
                                }catch(IOException ex){
                                    System.out.println("cannon connect");
                                }
                            } else if (s[0].equalsIgnoreCase("list")){
                                System.out.println("id:  IP address               Port N");
                                if (list.isEmpty())  
                                { 
                                    System.out.println("List is empty"); 
                                } 
                                else
                                {   
                                    list.forEach((key, value) -> System.out.println(key + ":  " + value[0] +"               "+value[1]));
                                }
                            } else if (s[0].equalsIgnoreCase("terminate")){
                                if(list.containsKey(Integer.parseInt(s[1]))){
                                PrintWriter pr1 = new PrintWriter(listSocket.get(Integer.parseInt(s[1])).getOutputStream(), true);
                                pr1.println("terminate " + InetAddress.getLocalHost().getHostAddress() + " " + server.getLocalPort());
                                list.remove(Integer.parseInt(s[1]));
                                listSocket.get(Integer.parseInt(s[1])).close();
                                listSocket.remove(Integer.parseInt(s[1]));
                                } else {
                                    System.out.println(" ID is not in the list");
                                }
                              
                            } else if (s[0].equalsIgnoreCase("send")){
                                if(list.containsKey(Integer.parseInt(s[1]))){
                                    out1 = new PrintWriter(listSocket.get(Integer.parseInt(s[1])).getOutputStream(), true);
                                    out1.println(list.get(Integer.parseInt(s[1]))[0] + " " + s[2]);
                                } else {
                                    System.out.println(" ID is not in the list");
                                }
                            } else if (s[0].equalsIgnoreCase("exit")){
                                
                                Iterator<Entry<Integer, String[]>> entryIt = list.entrySet().iterator();
                                while(entryIt.hasNext()){
                                        Entry<Integer, String[]> entry = entryIt.next();
                                        int k = entry.getKey();
                                        PrintWriter pr1 = new PrintWriter(listSocket.get(k).getOutputStream(), true);
                                        pr1.println("exit " + InetAddress.getLocalHost().getHostAddress() + " " + server.getLocalPort());
                                }
                                
                                list.clear();
                                listSocket.clear();
                                server.close();
                                
                                System.out.println(" Connection ended by server");
                                System.exit(0);
                                return;
                            }
                }while(true);
                
            } else if (Thread.currentThread() == t2){
                    //LISTEN TO NEW CLIENT
                do{ 
                    
                    client = server.accept();
                    
                    in2 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out = in2.readLine();
                    s = out.split(" ");
                    System.out.println("The connection to peer " + s[0] + " is successfully established true;");
                    
                    
                    //CONNECT BACK TO CLIENT
                    if(t){
                        client = new Socket(s[0], Integer.parseInt(s[1]));
                        PrintWriter pr1 = new PrintWriter(client.getOutputStream(), true);
                        pr1.println(InetAddress.getLocalHost().getHostAddress() + " " + server.getLocalPort());
                    }else{
                        t = true;
                    }
                    
                    //UPDATE LIST
                    list.put(id, s);
                    listSocket.put(id, client);
                    id++;
                    
                    ServerThread runnable= new ServerThread(id, client, list, listSocket);
                    pool.execute(runnable);
                }while(true);
            }
        } catch (IOException e) {
        }
    }
    
         private static class ServerThread implements Runnable {
            int id;
            Socket client;
            Map<Integer,String[]> list;
            Map<Integer,Socket> listSocket;
            ServerSocket server;
            
            BufferedReader in1, in2; 
            PrintWriter out1;
            String[] s;
            String c, in, out;
            
            ServerThread(int id, Socket client, Map<Integer,String[]> list, Map<Integer,Socket> listSocket) throws IOException {
                this.id=id;
                this.client=client;
                this.list = list;
                this.listSocket = listSocket;
            }

            @Override
            public void run() {
                try {
                do{     //Read string from server
                        in2 = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        out = in2.readLine();
                        s = out.split(" ",2);
                        
                        
                        if(s[0].equalsIgnoreCase("terminate") || s[0].equalsIgnoreCase("exit")){
                            
                            s = out.split(" ", 3);
                            System.out.println("Peer " + s[1] + " terminates the connection");

                            Iterator<Entry<Integer, String[]>> entryIt = list.entrySet().iterator();
                            while(entryIt.hasNext()){
                                Entry<Integer, String[]> entry = entryIt.next();
                                if (entry.getValue()[1].equals(s[2])) {
                                        int k = entry.getKey();
                                        list.remove(k);
                                        listSocket.get(k).close();
                                        listSocket.remove(k);
                                        
                                }
                            }

                    }else{
                        System.out.println("Message received from " +s[0]+ ": \n\"" + s[1] + "\"");
                    }
                }while(true);
                }catch (IOException e) {
                }
            } 
    }
         
    public static void main(String[] args) throws IOException {
        ChatApp chat1 = new ChatApp(Integer.parseInt(args[0]));
    }
}
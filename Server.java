//Tasnim Nehal (tn1078)
//reused sample code given in net1 and net2 files in resources
/*

IMPORTANT NOTE: 

I assumed that after a user joins that user wont be able to see the messages that were sent before he connected however this is easily remedied by maintaining a static String buffer in
the Server class which will store each message sent from the client and write it back to the all clients which will then update the chatbox with the buffer of all messages on client side. The code for this can
be found commented out in lines 83-101 of this file

I also assumed that if one user has a name then other users cant also use that name so there are extra guards in place to enforce that and disconnecting frees up that name figured this was alright as this is an 
anonymous chat forum
*/
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    
    static int portNum=5190;
    static ArrayList<ProcessConnection> connections = new ArrayList<ProcessConnection>();
    static HashMap<String, Integer> names = new HashMap<String, Integer>();
    public static void main(String[] args) {
        ServerSocket ss = null;
        int id=0;
        try{
            ss = new ServerSocket(portNum);
            //System.out.println("Waiting for connections on port number: "+portNum);
            while (true){
                Socket client = ss.accept(); //Program will wait here for a LONG time!
                ProcessConnection conn = new ProcessConnection(id++,client);
                connections.add(conn);
                conn.start();
            }
            
        }
        catch(IOException e){ //System.out.println("IOError: "+e.toString()); System.exit(1);
    }
    }
    
}

class ProcessConnection extends Thread{
    //the buffer will contain all the messages being sent in the chat and will be written to each connection so anyone that just accessed the chat can see the message history
    static String buffer = ""; 
    int id;
    String name = "";
    Socket client;
    DataOutputStream sout;
    DataInputStream sin;
    ProcessConnection(int newid, Socket newclient){id=newid; client=newclient;}
    public void run(){
    
        //while (true){
        try{
            //System.out.println("Connection from: " +client.getInetAddress().toString()+" client "+id);
            //I was having issues reading and writring strings with the scanner and printstream classes so i used the IOstream instead
            sout = new DataOutputStream(client.getOutputStream());
            sin = new DataInputStream(client.getInputStream());
            //sout.print("Welcome to my echo server!\r\n");

            //loop to check if a clients desired name is already being used or not
            while (name.equals("")){
                String temp = sin.readUTF();
                if(Server.names.containsKey(temp)){
                    sout.writeUTF("false");
                }
                else {
                    sout.writeUTF("true");
                    name = temp;
                    Server.names.put(name, 0);
                    break;
                }
            }

            String line="";
            //exit mechanism, only possible by client hitting disconnect button on client end
            while (!line.equalsIgnoreCase("EXIT")){
                line = sin.readUTF();
                if (line.equalsIgnoreCase("EXIT")){
                    break;
                }
                /*
                if (!line.equalsIgnoreCase("EXIT")){
                    if (buffer.equals("")){
                        buffer = line;
                    }
                    else {
                        buffer += line;
                    }
                    //System.out.println("Client ("+id+") Said: "+line);
                    //sout.print(line+"\r\n");
            
                    //buffer += "\n";
                    //System.out.print(buffer);
                    for(ProcessConnection i : Server.connections){
                        if (!i.client.isClosed()) {i.sout.writeUTF(buffer);}
                    }
                    sout.writeUTF(buffer);
                }
                */
                for(ProcessConnection i : Server.connections){
                    if (!i.client.isClosed()) {i.sout.writeUTF(line);}
                }
            }
            //sout.print("Goodbye!\r\n");
            //System.out.println("Client ("+id+") Disconnected");
            //Closing all the streams and updating the static containers
            client.close();
            Server.names.remove(name);
            sout.close();
            sin.close();
        }
        catch(IOException e){
            
        }
    }
}

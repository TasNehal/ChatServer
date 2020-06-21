//Tasnim Nehal (tn1078)
//reused sample code given in net1 and net2 files in resources
/*

IMPORTANT NOTE: 

Exiting before the client is connected to anything is fine, after that I added code so that closing the jframe will also close all the socket and streams beforehand
Also added a disconnect button once the client has access to type in the chat which does the same thing but doesnt immediately exit (the extra code for closing a jframe is a backup)
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client {
    static int portNum;
    static String address;
    static Socket client;
    static Boolean connectFlag = true;
    static Boolean nameFlag = true;
    static String username;
    static DataOutputStream sout;
    static DataInputStream sin;
    static JFrame jf;

    public static void main(String[] args) {

        jf = new JFrame("Chat");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setSize(800, 700);
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridLayout(2, 1, 100, 100));
        JPanel inner = new JPanel();
        // user will first be prompted to select
        JTextField field = new JTextField(16);
        JLabel prompt = new JLabel(
                "Enter the address and port number of the chat seperated by commas in the field (no spaces) like so: localhost,5190");
        JButton submit = new JButton("Enter");
        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // retrieve the input and check if its valid
                    String[] submission = field.getText().split(",");
                    portNum = Integer.parseInt(submission[1]);
                    address = submission[0];
                    client = new Socket(address, portNum);
                    if (client.isConnected()) {
                        sout = new DataOutputStream(client.getOutputStream());
                        sin = new DataInputStream(client.getInputStream());
                        // System.out.println("you in");
                        connectFlag = false;
                    } else {
                        prompt.setText(
                                "ERROR, reenter the address and port number of the chat seperated by commas in the field (no spaces): localhost,5190");
                        field.setText("");
                    }
                } catch (Exception ex) {
                    prompt.setText(
                            "ERROR, reenter the address and port number of the chat seperated by commas in the field (no spaces): localhost,5190");
                    field.setText("");
                    //System.out.println("Welp... that didn't work!");
                }
            }
        });
        inner.add(field);
        inner.add(submit);
        wrapper.add(inner);
        wrapper.add(prompt);
        jf.add(wrapper);
        jf.setVisible(true);

        // pauses until connection to chat server is made to allow for JFrame to change
        // afterwards
        while (connectFlag) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                return;
            }
        }

        // connection made so now prompt for username
        prompt.setText("You're connected to " + address + " on port " + portNum + " , enter a username now");
        field.setText("");
        // replace old action listener
        submit.removeActionListener(submit.getActionListeners()[0]);
        submit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // retrieve the input and check if its valid
                String temp = field.getText();
                try {
                    sout.writeUTF(temp);
                    if (sin.readUTF().equals("false")){
                        prompt.setText("The username " + temp + " is already taken on this server, use another name");
                        field.setText("");
                    }
                    else{
                        username = temp;
                        nameFlag = false;
                        field.setText("");
                    }
                } catch (IOException e1) {
                }
            }
        });

        // pauses until username is selected then changes to the chat screen
        while (nameFlag) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                return;
            }
        }

        // reset the current JFrame to transition into the actual chat
        // System.out.println(username);
        jf.getContentPane().invalidate();
        jf.getContentPane().removeAll();
        jf.getContentPane().revalidate();
        jf.getContentPane().repaint();

        JPanel screen = new JPanel();
        screen.setLayout(new GridLayout(2, 1, 100, 100));

        // make the chat logs scrollable
        JTextArea chat = new JTextArea();
        chat.setEditable(false);
        JScrollPane scroll = new JScrollPane(chat);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel internal = new JPanel();
        JTextField messagePane = new JTextField(60);
        //button to cleanly close the streams and exit server
        JButton leave = new JButton("Disconnect");
        leave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                messagePane.setText("");
                messagePane.setEditable(false);
                chat.setText("You are disconnected, close the window and restart the application to reconnect");
                try {
                    sout.writeUTF("EXIT");
                    sout.close();
                    sin.close();
                    client.close();
                } catch (IOException e1) {
                }
                // System.out.println(message);
            }
        });

        // button to messages to the server
        JButton chatSubmit = new JButton("Enter");
        chatSubmit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // retrieve the input and send to server
                String message = username + ": " + messagePane.getText() + "\n";
                // chat.setText(chat.getText() + message);
                messagePane.setText("");
                try {
                    sout.writeUTF(message);
                } catch (IOException e1) {
                }
                // System.out.println(message);
            }
        });
        //Thread to continously update the chat log as long as client isnt closed
        class UpdateScreen extends Thread {
            public void run() {
                try {
                while(!client.isClosed()){
                    String buffer = sin.readUTF();
                    //if using a system where all message logs are stored in server then just the next line would be enough. however I made it so a user can only see messages sent after he connected to the server
                    //chat.setText(buffer);
                    chat.setText(chat.getText() + buffer);
                    messagePane.setText("");
                }                    
                } catch (IOException e) {
                    
                }
            }
        }
        new UpdateScreen().start();

        jf.setTitle("Username: " + username);
        internal.add(messagePane);
        internal.add(chatSubmit);
        internal.add(leave);
        screen.add(scroll);
        screen.add(internal);
        jf.add(screen);
        jf.setVisible(true);
                //prevent clients from from creating errors by closing all sockets and streams before they exit the application (just in case user does not hit the Disconnect button)
                jf.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        if (!client.isClosed()){
                            try {
                                if(!nameFlag){
                                    sout.writeUTF("EXIT");
                                }
                                sout.close();
                                sin.close();
                                client.close();
                            } catch (IOException e1) {
                            }
                        }
                        System.exit(0);
                    }
                });



       

        

        






        /* try{
            Socket s = new Socket("localhost",5190);
            if (s.isConnected()){
                while(true){
                    DataOutputStream sout = new DataOutputStream(s.getOutputStream());
                    DataInputStream sin = new DataInputStream(s.getInputStream());
                    try (Scanner scanner = new Scanner(System.in)) {

                        System.out.print("Enter something : ");
            
                        String input = scanner.nextLine();  // Read user input
            
                        System.out.println("User Input : " + input);
            
                    }
                }
            }
            else{
                System.out.println("Socket COnnection Failed!");
            }
        }
        catch(IOException e){
            System.out.println("Welp... that didn't work!");
        } */
    }
    
}

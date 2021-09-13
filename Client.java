import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Socket client;
    private static OutputStream outToServer;
    private static DataOutputStream out;
    private static InputStream inFromServer;
    private static DataInputStream in;
    public static void main(String[] args) throws IOException {
        boolean turn = false;
        boolean gameOver = false;
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        String typing = "firstmessage";
        String msg;
        final int WAITING = 0;
        final int RESPOND = 1;
        final int POSTING = 2;
        int state = WAITING;
        boolean turnPost = false;
        try {
            client = new Socket(serverName, port);
            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            outToServer = client.getOutputStream();
            out = new DataOutputStream(outToServer);
            //out.writeUTF("Hello from " + client.getLocalSocketAddress());
            inFromServer = client.getInputStream();
            in = new DataInputStream(inFromServer);
//            System.out.println("Server says " + in.readUTF());
            while (!gameOver) {

                while (in.available() > 0) {
                    msg = in.readUTF();
                    System.out.println(msg);
                    if (msg.equals("It is your turn!" ) ||  msg.equals("Would you like another Card? y/n ")) {
                        turn = true;
                        state = RESPOND;
                    } else if (msg.equals("Game Over!")) {
                        gameOver = true;
                    }
                }
                while (turn) {
                    if (state == WAITING) {
                        //System.out.println("came back here");
                        while (in.available() > 0) {
                            msg = in.readUTF();
                            System.out.println(msg);
                            if (msg.equals("Would you like another Card? y/n ") || msg.equals("Do you want this Ace to be worth 11? y/n (if n, ace will be worth 1)") ) {
                                state = RESPOND;
                                turn = true;
                            }
                        }
                    }
                    if (state == RESPOND) {
                        //System.out.println("I am In Here");
                        typing = "";
                        while (!typing.equals("y") && !typing.equals("n")) {
                            typing = keyboard.nextLine();
                            if (typing.equals("y")) {
                                out.writeUTF(typing);
                                //System.out.println("now im here");
                                state = WAITING;
                            } else if (typing.equals("n")) {
                                out.writeUTF(typing);
                                turn = false;
                                state = WAITING;
                            } else {
                                System.out.println("Please Input y or n");
                            }
                        }
                    }
                }
                if (in.available() > 0) {
                    msg = in.readUTF();
                    if (msg.equals("Game Over")) {
                        System.out.println(msg);
                        gameOver = true;
                        state = POSTING;
                    }
                }
            }

            if (state == POSTING) {
                while (in.available() > 0) {
                    msg = in.readUTF();
                    System.out.println(msg);
                    System.exit(0);

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            out.close();
            in.close();
            client.close();
        }
    }
}

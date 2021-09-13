package ca.mohawk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Player Object, that holds their own hands and run method. THe players will be running off this method in order to communicate with the black jack game. It holds a reference to the Server Object as the
 * server holds the board information
 */
public class Player extends BasePlayer {
    private Socket socket; //The players Socket
    private int playerstate = 1; //Player State
    private final int WAITING = 1; //WAITING CONSTANT
    private final int GOTRESPONSE = 2; // RESPONSE CONSTANT
    private DataInputStream in; // input from client
    private DataOutputStream out; //output to client
    private SkeletonServer server; // The server reference
    private boolean boardPosted = false; // Flag to see if board has been posted yet
    private boolean turnPost = false; //Flag to see if turn message was posted
    private boolean aceCheckComplete = false; // Flag to see if needs to check ace logic
    private final int CHECKWAIT = 1; //ACE CHECK WAITING FOR RESPONSE CONSTANT
    private final int CHECKRESPOND = 2; // ACE CHECK FOR CHECKING THE RESPONSE
    private int checkState = CHECKWAIT; //Check state
    private int id; // Player ID

    /**
     * Instantiates a new Player.
     *
     * @param socket     the socket
     * @param serverData the server data
     * @throws IOException the io exception
     */
    public Player(Socket socket, SkeletonServer serverData) throws IOException {
        super();
        server = serverData;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        id = server.getPlayerNumber();
        server.nextPlayerNumber();
    }

    /**
     * The run function that holds all the game logic for the player
     */
    @Override
    public void run() {
        // Runs as long as its the players turn and if the game is not over
        while (server.getGameState() != SkeletonServer.GAMEOVER && server.getGameState() == SkeletonServer.PLAYERTURN) {
            try {
                //Post the Board
                if (!boardPosted) {
                    out.writeUTF("Your Hand");
                    out.writeUTF("---------");
                    for (Card c : hand) {
                        out.writeUTF(c.print());
                    }
                    out.writeUTF("You are holding " + getAceCount() + " ACES");
                    if (hasAce()) {
                        aceCheckComplete = false;
                    } else {
                        aceCheckComplete = true;
                    }
                    out.writeUTF(" ");

                    out.writeUTF("The Table");
                    out.writeUTF("You are Player " + getPlayerId());
                    out.writeUTF("---------");
                    String board = server.getBoard();
                    out.writeUTF(board);
                    out.writeUTF("---------");
                    out.writeUTF(". . . It is Not Your Turn Yet! . . .");
                    boardPosted = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Run While its the players turn
            while (getTurn()) {
                //System.out.println("Player Turn");
                String line = "";
                try {
                    // post Turn Message
                    if (!turnPost) {
                        out.writeUTF("It is your turn!");
                        turnPost = true;
                        playerstate = GOTRESPONSE;
                    }
                    // Check if Ace logic needs to be run
                    while (!aceCheckComplete) {
                        out.writeUTF("You are holding " + getAceCount() + " ACES");
                        int count = 0;
                        int aceNum = 1;
                        // Goes through every card in the players hand
                        while (count < hand.size()) {
                            // If the card is an ace ask if the player wants it to be worth 1 or 11, switch states to waiting for response
                            if (hand.get(count).ace()) {
                                if (checkState == CHECKWAIT) {
                                    out.writeUTF("Ace " + aceNum);
                                    out.writeUTF("Do you want this Ace to be worth 11? y/n (if n, ace will be worth 1)");
                                    checkState = CHECKRESPOND;
                                }
                                //Gets response and calculate values accordingly
                                if (checkState == CHECKRESPOND) {
                                    while (in.available() > 0) {
                                        //System.out.println("was here");
                                        line = in.readUTF();
                                        // Set ace value to 11 and update hand value, swaps the state to waiting
                                        if (line.equals("y")) {
                                            out.writeUTF("value of Ace " + aceNum + " is now 11!");
                                            aceValues.set(aceNum - 1, 11);
                                            updateValue();
                                            out.writeUTF("New hand Value is : " + handValue);
                                            count++;
                                            aceNum++;
                                            checkState = CHECKWAIT;
                                        // Sets ace value to 1, updates hand value and swap state to waiting again
                                        } else if (line.equals("n")) {
                                            out.writeUTF("Ace is set to 1");
                                            aceValues.set(aceNum - 1, 1);
                                            updateValue();
                                            out.writeUTF("New hand Value is : " + handValue);
                                            count++;
                                            aceNum++;
                                            checkState = CHECKWAIT;
                                        }
                                    }
                                }
                                // Move on to next card
                            } else {
                                count++;
                            }
                        }
                        //Ace Logic done move onto turn
                        aceCheckComplete = true;
                    }
                    //Give a final update to hand value just incase
                    updateValue();

                    //Check if player is bust, if not proceed with turn
                    if (isBust()) {
                        out.writeUTF("Your Hand went over 21. You have Busted. Please wait for the game to end.");
                        out.writeUTF("Your hand value is : " + handValue);
                        playerstate = GOTRESPONSE;
                        setTurn(false);
                    }else {
                        // SECTION FOR IF PLAYER NEEDS TO HIT/STAY
                        // Ask player if they want a card
                        if (playerstate == GOTRESPONSE) {
                            out.writeUTF("Would you like another Card? y/n ");
                            playerstate = WAITING;
                        }
                        //Process response
                        if (playerstate == WAITING) {
                            while (in.available() > 0) {
                                //System.out.println("was here");
                                line = in.readUTF();
                                //Deal player a card and calculate hand value
                                if (line.equals("y")) {
                                    server.getDeck().deal(this);
                                    String nextCard = getLastCard();
                                    out.writeUTF("Next card is : " + nextCard);
                                    //Check if they went bust
                                    if (isBust()) {
                                        out.writeUTF("Your Hand went over 21. You have Busted. Please wait for the game to end.");
                                        out.writeUTF("Your hand value is : " + handValue);
                                        playerstate = GOTRESPONSE;
                                        setTurn(false);
                                        //Give player a chance to redo Aces
                                    } else if (hasAce()) {
                                        aceCheckComplete = false;
                                        playerstate = GOTRESPONSE;
                                        checkState = CHECKWAIT;
                                    }
                                    //Switch to Gotresponse and ask players again
                                    playerstate = GOTRESPONSE;
                                    //End turn
                                } else if (line.equals("n")) {
                                    out.writeUTF(" . . . Your Turn is Over! Please Wait for other Players . . .");
                                    playerstate = GOTRESPONSE;
                                    setTurn(false);
                                }
                            }
                        }
                    }
                } catch (SocketTimeoutException s) {
                    System.out.println("Socket timed out!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    }
            }

        //Posts the Final results to the Client
        while (server.getGameState() == SkeletonServer.GAMEOVER) {
            if (getTurn()) {
                try {
                    out.writeUTF("Game Over");
                    String board = server.getBoard();
                    out.writeUTF(board);
                    out.writeUTF("---------");
                    out.writeUTF(server.getWinnerText());
                    setTurn(false);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        //out.writeUTF("Thank you for connecting to " + socket.getLocalSocketAddress() + "\nGoodbye!");
                        out.close();
                        in.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Gets player id.
     *
     * @return the player id
     */
    public int getPlayerId() {
        READLOCK.lock();
        try {
            return id;
        } finally {
            READLOCK.unlock();
        }
    }


}

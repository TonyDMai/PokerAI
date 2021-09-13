package ca.mohawk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  I, Doan Mai, student number 000748737,  Certify that all code submitted is my own work; that I have not copied it from any other source. I also certify that I have not allowed my work to be copied by others.
 *
 *  This is the primary class for handling server side applications, it checks what the game state is for the black jack game and will check which respective players turn it is.
 *
 */
public class SkeletonServer extends Thread {

    private int gameState = 0;

    /**
     * The constant CONNECTING.
     */
    public static final int CONNECTING = 0;
    /**
     * The constant DEALING.
     */
    public static final int DEALING = 1;
    /**
     * The constant PLAYERTURN.
     */
    public static final int PLAYERTURN = 2;
    /**
     * The constant GAMEOVER.
     */
    public static final int GAMEOVER = 3;

    private static int expectedPlayers; // amount of players expected to play
    private static ArrayList<BasePlayer> players = new ArrayList<BasePlayer>(); // List of all players that are in the game
    private static Deck deck; // The Deck of cards that the game uses

    private String board = ""; // String for the board to output to all players
    private int playerNumber = 1; //Current Player Number
    private String winnerText; //Output message for who wins the game

    private static final ReadWriteLock TURNLOCK = new ReentrantReadWriteLock(); // Re-entrant lock that is used for reading and writing to variables in the game
    private final Lock READLOCK = TURNLOCK.readLock(); //Read lock
    private final Lock WRITELOCK = TURNLOCK.writeLock(); // Write Lock

    /**
     * Get winner text string.
     *
     * @return the string
     */
    public String getWinnerText(){
        READLOCK.lock();
        try{
            return winnerText;
        }finally {
            READLOCK.unlock();
        }
    }

    /**
     * Set winner text.
     *
     * @param text Winner Text
     */
    public void setWinnerText(String text){
        WRITELOCK.lock();
        try{
            winnerText = text;
        }finally {
            WRITELOCK.unlock();
        }
    }

    /**
     * Get game over boolean.
     *
     * @return game Over State
     */
    public boolean getGameOver(){
        READLOCK.lock();
        try{
            return gameState == GAMEOVER;
        }
        finally{
            READLOCK.unlock();
        }
    }

    /**
     * Get player number int.
     *
     * @return the int
     */
    public int getPlayerNumber(){
        READLOCK.lock();
        try{
            return  playerNumber;
        }
        finally {
            READLOCK.unlock();
        }
    }

    /**
     * Sets Next player number.
     */
    public void nextPlayerNumber(){
        WRITELOCK.lock();
        try{
            playerNumber++;
        }finally {
            WRITELOCK.unlock();
        }
    }

    /**
     * Gets the deck.
     *
     * @return the deck
     */
    public Deck getDeck(){
        READLOCK.lock();
        try{
            return deck;
        }
        finally {
            READLOCK.unlock();
        }
    }

    /**
     * Set game state.
     *
     * @param state the current game state
     */
    public void setGameState(int state){
        WRITELOCK.lock();
        try{
            gameState = state;
        }
        finally{
            WRITELOCK.unlock();
        }

    }

    /**
     * Get game state int.
     *
     * @return the int
     */
    public int getGameState(){
        READLOCK.lock();
        try{
            return gameState;
        }
        finally {
            READLOCK.unlock();
        }
    }

    /**
     * Get board string.
     *
     * @return the string
     */
    public String getBoard(){
        READLOCK.lock();
        try{
            return board;
        }finally{
            READLOCK.unlock();
        }
    }

    /**
     * Set board.
     */
    public void setBoard(){
        WRITELOCK.lock();
        try{
            int count = 1;
            for (BasePlayer player : players){
                if (player instanceof Player){
                    board += "Player " + count + " : " + player.returnHand() + "\n";
                    count++;
                }
                else if (player instanceof Dealer) {
                    board += "Dealer : " + player.returnHand();
                }
            }
        }
        finally{
            WRITELOCK.unlock();
        }
    }

    /**
     * Set game over board.
     */
    public void setGameOverBoard(){
        WRITELOCK.lock();
        try{
            board = "";
            int count = 1;
            for (BasePlayer player : players){
                if (player instanceof Player){
                    board += "Player " + count + " : " + player.returnHand() + " = " + player.handValue + "\n";
                    count++;
                }
                else if (player instanceof Dealer) {
                    board += "Dealer : " + player.returnHand() + " = " + player.handValue;
                }
            }
        }
        finally{
            WRITELOCK.unlock();
        }
    }

    /**
     * Instantiates a new Skeleton server.
     *
     * @throws IOException the io exception
     */
    public SkeletonServer() throws IOException { }

    /**
     * The Run method for the game, this is run on the game thread that is initialized once every player has connected.
     * This method will deal with checking game states and will assist in directing each player
     */
    public void run() {

        //Deals the Cards to Users
        if (gameState == DEALING) {
            System.out.println("Dealing Cards");
            for (int i = 0; i < 2; i++) {
                for (BasePlayer player : players) {
                    deck.deal(player);
                    if (player instanceof Dealer && i == 1){
                        player.setDown();
                    }
                }
            }
            setBoard();
            setGameState(PLAYERTURN);
        }
        //START A NEW THREAD FOR EACH PLAYER, Each player will run their own run thread which has all the game logic inside
        for (BasePlayer player : players) {
            player.start();
        }
        //System.out.println(getBoard());
        // Checks if its the player turn game State , iterate through each player and let them play. If its their turn stall the game until they finish
        if (getGameState() == PLAYERTURN) {
            // Goes through each player
            for (BasePlayer player : players) {
                player.setTurn(true);
                while (player.getTurn()) ; // Busy Waiting for players to finish
            }
            setGameState(GAMEOVER); // Once all players have gone set game state to game Over
        }
        // Output the final results and winner
        if (getGameState() == GAMEOVER) {
            setGameOverBoard(); //Sets the new final game board and shows all dealer cards as well
            BasePlayer winner = null; // Holds the current winner temporarily
            ArrayList<String> winners = new ArrayList<>(); // List used to Check for ties
            //Goes through each player to compare
            for (BasePlayer player : players) {
                //Default to Current Player is winner if they are not bust and no winner detected
                if (winner == null && !player.isBust()){
                    winner = player;
                    //Gets text value for the winner String
                    if (player instanceof Player) {
                        winners.add("Player" + ((Player) player).getPlayerId());
                    }else if (player instanceof Dealer){
                        winners.add("Dealer");
                    }
                    continue;
                }
                //If the current Players Hand is better than the current winner
                if (winner != null && (player.handValue > winner.handValue) && !player.isBust()){
                    winner = player;
                    if (player instanceof Player) {
                        winners.set(0, "Player" + ((Player) player).getPlayerId());
                    }
                    else if (player instanceof  Dealer){
                        winners.set(0,"Dealer");
                    }
                }
                // If the current players hand is tied with the current winner
                else if (winner != null && player.handValue == winner.handValue){
                    if (player instanceof Player) {
                        winners.add("Player" + ((Player) player).getPlayerId());
                    }
                    else if (player instanceof Dealer){
                        winners.add("Dealer");
                    }
                }
            }
            // If there are tied hands output the tied text
            if (winners.size() > 1){
                String output = "";
                for (String win : winners){
                    output += win + ", ";
                }
                setWinnerText("The players : " + output + "Tied");
            }
            // If there are no winners
            else if (winners.size() == 0){
                setWinnerText("Everyone Busted! No one Wins!");
            }
            //Otherwise output 1 winner
            else {
                setWinnerText(winners.get(0) + " Wins!");
            }
            //Go through each player and post the end results
            for (BasePlayer player : players){
                if (player instanceof Player) {
                    player.setTurn(true);
                    while (player.getTurn()) ;
                }
            }
            //Post to Server
            System.out.println(getBoard());
            System.out.println(getWinnerText());
            System.exit(0);
            //setGameState(4);
        }

    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        SkeletonServer game = new SkeletonServer(); // Game Thread
        //Attempt to get number of players
        try {
            expectedPlayers = Integer.parseInt(args[0]);
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Must have at least 1 or 2 players");
        }catch (NumberFormatException e) {
            System.out.println("Parameter Input must be a number");
        }

        int port = 0;
        ServerSocket mySocket = new ServerSocket(port);

        // Waits for all players to connect
        while (players.size() < expectedPlayers) {
            System.out.println("Waiting for client on port "
                    + mySocket.getLocalPort() + "...");
            Socket server = mySocket.accept(); //blocking
            try {
                players.add(new Player(server, game));
                //players.add(new Player(server));
            }
            finally{
            }
            //Once all players are connected add a dealer to the game, and create a deck. Finally start the game thread and set state to dealing
            if (players.size() == expectedPlayers) {
                players.add(new Dealer(game));
                System.out.println("All Players are connected ");

                deck = new Deck();
                //deck.shuffle();
                deck.readFromFile();

                game.start();
                game.setGameState(DEALING);
            }
        }
    }
}

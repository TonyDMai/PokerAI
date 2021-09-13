package ca.mohawk;

import java.io.IOException;
import java.util.Scanner;

/**
 * Dealer object, basically the same as a player but does not need Input and output streams.
 */
public class Dealer extends BasePlayer{
    private SkeletonServer server;

    private boolean boardPosted = false; // Flag for if board is posted on server
    private boolean waitingPosted = false; //Flag for if the command has been given yet
    private boolean enterClicked = false; // Flag for if the user has clicked enter to play the dealer yet
    private boolean play = true; // Flag to see if the dealer can play

    private Scanner scan = new Scanner(System.in); // Scanner for user input


    /**
     * Instantiates a new Dealer.
     *
     * @param server the server, reference to the server, for board information
     */
    public Dealer(SkeletonServer server){
        super();
        this.server = server;

    }
    @Override
    public void run() {
        // Only play when its dealers turn
        while (!server.getGameOver() && server.getGameState() == SkeletonServer.PLAYERTURN) {
            // Post Board to server
            if (!boardPosted) {
                setDown();
                System.out.println("Dealer Hand");
                System.out.println("-----------");
                for (Card c : hand) {
                    System.out.println(c.print());
                }
                boardPosted = true;
            }
            // Wait for turn and then play
            while (getTurn()) {
                // User input command
                if (!waitingPosted){
                    System.out.println("Press Enter to Play Dealer");
                    waitingPosted = true;
                }
                //Wait for enter to be pressed
                while (!enterClicked) {
                    scan.nextLine();
                    enterClicked = true;
                }
                // play the dealer
                while (play){
                    //Checks if dealer has an ace and runs ace logic
                    if (hasAce() && !isBust()){
                        aceCheck();
                    }
                    // if hand over 16 then end turn
                    if (handValue > 16){
                        play = false;
                        setTurn(false);
                    }
                    // if bust end turn
                    else if (isBust()){
                        play = false;
                        setTurn(false);
                        //otherwise draw a card then check ace logic if drew an ace
                    }else{
                        server.getDeck().deal(this);
                        if (hasAce() && !isBust()){
                            aceCheck();
                        }
                        System.out.println("Dealer's Next Card : " + getLastCard());

                    }
                }
            }
        }
        //Post game over Messages
        while (server.getGameState() == SkeletonServer.GAMEOVER) {

            if ( getTurn()) {
                try {
                    System.out.println("Game Over");
                    String board = server.getBoard();
                    System.out.println(board);
                    System.out.println();
                    System.out.println(server.getWinnerText());
                    setTurn(false);
                } finally {
                        System.out.println("Game is now Over. Aborting");
                        //System.exit(0);
                }
            }
        }
    }

    /**
     * Checks for ace logic.
     */
    public void aceCheck(){
        int aceCount = 0;
        // Goes through each card in the hand
        for(Card c : hand) {
            if (c.ace()) {
                // If theres only 2 cards in the hand and the current value is 11 (card is a 10,J,Q,K and Ace) set ace to 11 and continue;
                if (!isBust() && hand.size() <= 2 && handValue == 11) {
                    aceValues.set(aceCount,11);
                    updateValue();
                    break;
                }
                // If the hand value is less than 10 set the ace to 11
                if (!isBust() && handValue <= 10) {
                    aceValues.set(aceCount, 11);
                    aceCount++;
                    updateValue();
                    continue;
                }
                //if the hand is less than 20 and greater than 16 set ace value to 1
                if (!isBust() && handValue <= 20 && handValue >= 16) {
                    aceValues.set(aceCount, 1);
                    aceCount++;
                    updateValue();
                    continue;
                }
            }
        }
    }
}

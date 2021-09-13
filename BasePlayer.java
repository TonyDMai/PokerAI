package ca.mohawk;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The type Base player.
 */
public class BasePlayer extends Thread{
    /**
     * Re-entrant lock for reading and writng
     */
    protected final ReadWriteLock TURNLOCK = new ReentrantReadWriteLock();
    /**
     * The Readlock.
     */
    protected final Lock READLOCK = TURNLOCK.readLock();
    /**
     * The Writelock.
     */
    protected final Lock WRITELOCK = TURNLOCK.writeLock();
    /**
     * List of players cards, representing their hand
     */
    protected List<Card> hand;
    /**
     * Current Hand Value
     */
    protected int value;
    /**
     * Whether the player hand value is over 21 or not
     */
    protected boolean bust;
    /**
     * Flag for if its the players turn
     */
    protected boolean isTurn;
    /**
     * The amount of aces in the players hand
     */
    protected int aceCount;
    /**
     * The Hand value.
     */
    protected int handValue;
    /**
     * Flag on whether to calculate aces or not
     */
    protected boolean addAce = false;
    /**
     * List of all the ace values in the hand
     */
    protected ArrayList<Integer> aceValues;


    /**
     * Instantiates a new Base player.
     */
    public BasePlayer(){
        hand = new ArrayList<Card>();
        handValue = 0;
        value = 0;
        bust = false;
        isTurn = false;
        aceCount = 0;
        aceValues = new ArrayList<>();
    }

    /**
     * Get turn boolean.
     *
     * @return the boolean
     */
    public boolean getTurn(){
        READLOCK.lock();
        try{
            return isTurn;
        }
        finally {
            READLOCK.unlock();
        }
    }


    /**
     * Set player turn.
     *
     * @param turn the turn
     */
    public void setTurn(boolean turn){
        WRITELOCK.lock();
        try{
            isTurn = turn;
        }
        finally{
            WRITELOCK.unlock();
        }
    }

    /**
     * Deals a card to the player, if its an ace than add a value to the ace values list and increment the amount of aces by 1
     *
     * @param card the card
     * @return the card
     */
    public Card hit(Card card){
        hand.add(card);
        value += card.getValue();
        if (card.getValue() == 1){
            aceCount++;
            aceValues.add(1);
        }
        if ( value > 21){
            bust = true;
        }
        return card;
    }

    /**
     * Get ace count int.
     *
     * @return the int
     */
    public int getAceCount(){
        return aceCount;
    }

    /**
     * Get hand.
     */
    public void getHand(){
        for (Card c :hand) {
            System.out.println( c.print());
        }
    }

    /**
     * Get card string.
     *
     * @param cardPos the card pos
     * @return the string
     */
    public String getCard(int cardPos){
        return hand.get(cardPos).print();
    }

    /**
     * Returns the cards in hand as a string
     *
     * @return the string
     */
    public String returnHand(){

        String cards = "";
        for (Card c :hand) {
            cards += c.print() + ", ";
        }
        return cards;
    }

    /**
     * Check if theres an ace in the hand
     *
     * @return the boolean
     */
    public boolean hasAce(){
        for (Card c : hand) {
            if (c.getValue() == 1){
                return true;
            }
        }
        return false;
    }

    /**
     * Update value of the hand.
     */
    public void updateValue(){
        handValue = 0;
        for (Card c : hand){
            int value = c.getValue();
            // if its an ace dont add to value and save value calculations for last
            if (value == 1){
                value = 0;
                addAce = true;
            }
            handValue += value;
        }
        // if theres an ace add the ace values afterwards
        if (addAce){
            for( Integer i : aceValues){
                handValue += i;
            }
            addAce = false;
        }
        // sets player to bust if their hand goes over 21
        if (handValue > 21){
            bust = true;
        }
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * Is bust boolean.
     *
     * @return the boolean
     */
    public boolean isBust() {
        return bust;
    }

    /**
     * Set bust.
     */
    public void setBust(){
        bust = true;
    }

    /**
     * Get last card string.
     *
     * @return the string
     */
    public String getLastCard(){
        return hand.get(hand.size() - 1).print();
    }

    /**
     * Set hidden visibility for dealer 2nd card
     */
    public void setDown(){
        hand.get(1).setHidden();
    }


}

package ca.mohawk;

/**
 * Card object
 */
public class Card {
    /**
     * Value of the card 2-10,
     */
    String value;
    /**
     * Card Suit
     */
    String suit;
    /**
     * Boolean for whether the card should be seen or not
     */
    Boolean hidden;
    /**
     * Boolean Flag for if the Ace should be considered an 11 value or not
     */
    boolean ace11 = false;

    /**
     * Instantiates a new Card.
     *
     * @param value the value
     * @param Suit  the suit
     */
    public Card(String value, String Suit){
        this.value = value;
        this.suit = Suit;
        this.hidden = false;
    }

    /**
     * Prints the Card Data
     *
     * @return the string
     */
    public String print(){
        String cardValue;
        if (hidden){
            cardValue = "XX";
        }else {
            cardValue = value + " of " + suit;
        }
        return cardValue;
    }

    /**
     * Get value int.
     * If it is a face card the value is 10, if its an ace its default value is 1
     * @return the int
     */
    public int getValue(){
        int cardValue;
        if (value == "Jack" || value == "Queen" || value =="King"){
            cardValue = 10;
        }else if (value == "Ace"){
            cardValue = 1;
        }else{
            cardValue = Integer.parseInt(value);
        }
        return cardValue;
    }

    /**
     * Sets card visibility to hidden.
     */
    public void setHidden() {
        this.hidden = !hidden;
    }

    /**
     * return if the card is an ace
     *
     * @return the boolean
     */
    public boolean ace(){
        if (value == "Ace"){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Set ace value flage to true, makes value 11.
     *
     * @param value the value
     */
    public void setAce11(boolean value){
        ace11 = value;
    }

}

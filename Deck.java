package ca.mohawk;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Deck Object that will hold a standard Playing Card Deck. Responsible for dealing to players and reading from a file
 */
public class Deck {
    /**
     * The Deck of cards.
     */
    public ArrayList<Card> deckOfCards;
    /**
     * Instantiates a new Deck. Creates each card from 2-10, then face cards
     */
    public Deck(){
        deckOfCards = new ArrayList<Card>();
        for (int i = 2; i < 11; i++){
            for (int j = 0; j < 4; j++){
                String suit = "";
                switch(j){
                    case 0:
                        suit = "Spades";
                        break;
                    case 1:
                        suit = "Clubs";
                        break;
                    case 2:
                        suit = "Diamonds";
                        break;
                    case 3:
                        suit = "Hearts";
                        break;
                }
                deckOfCards.add (new Card(Integer.toString(i),suit));
            }
        }
        for (int i = 0; i < 4; i++){
            String value = "";
            switch(i){
                case 0:
                    value = "Jack";
                    break;
                case 1:
                    value = "Queen";
                    break;
                case 2:
                    value = "King";
                    break;
                case 3:
                    value = "Ace";
                    break;
            }
            for (int j = 0; j < 4; j++){
                String suit = "";
                switch(j){
                    case 0:
                        suit = "Spades";
                        break;
                    case 1:
                        suit = "Clubs";
                        break;
                    case 2:
                        suit = "Diamonds";
                        break;
                    case 3:
                        suit = "Hearts";
                        break;
                }
                deckOfCards.add (new Card(value,suit));
            }
        }
        shuffle(); // Shuffles the deck after creation
    }

    /**
     * Shuffle the deck
     */
    public void shuffle(){
        Collections.shuffle(deckOfCards);
    }

    /**
     * Wrrite the deck to server
     */
    public void printdeck(){
        for (int i = 0; i < deckOfCards.size();i++){
            System.out.println(deckOfCards.get(i).print());
        }
    }

    /**
     * Deals to the player
     * Gives a card to the current player and updates their hand value
     * @param player current player
     */
    public void deal(BasePlayer player){
        player.hit(deckOfCards.get(0));
        //System.out.println("Player is dealt the " + deckOfCards.get(0).print());
        deckOfCards.remove(0);
        player.updateValue();
       // System.out.println("Dummy Line");
    }

    /**
     * Read from file to generate the deck
     */
    public void readFromFile(){
        String data = "";
        try{
            File myObj = new File("deck.txt");
            Scanner reader = new Scanner(myObj);
            while(reader.hasNextLine()){
                data = reader.nextLine();
            }
        }
        catch(FileNotFoundException e){
            System.out.println("File not Found.");
            e.printStackTrace();

        }
        //Resets the deck of cards
        deckOfCards.clear();
        //Parses each String value to the corresponding card
        String[] cards = data.split(",");
        for(int i = 0; i < cards.length;i++){
            String value = cards[i].substring(0,cards[i].length() - 1);
            switch(value){
                case "J":
                    value = "Jack";
                    break;
                case "Q":
                    value = "Queen";
                    break;
                case "K":
                    value = "King";
                    break;
                case "A":
                    value = "Ace";
                    break;
            }
            String suit = cards[i].substring(cards[i].length() - 1);
            switch(suit){
                case "C":
                    suit = "Clubs";
                    break;
                case "D":
                    suit = "Diamonds";
                    break;
                case "H":
                    suit = "Hearts";
                    break;
                case "S":
                    suit = "Spades";
                    break;
            }
            deckOfCards.add(new Card(value,suit));
        }
    }
}

package memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Player ADT to represent a player in the game
 * @author davidmagrefty
 *
 */

public class Player {
    private int score = 0;
    private PlayerState state = PlayerState.FRESH;
    private List<Card> cards = new ArrayList<Card>();
    private final String playerId;
    private boolean attemptedToFlip = false;
    
    /*
     * AF(playerId, state, score, cards, attemptedToFlip) = The player
     *      represented by playerId who's current game play is state, where state is
     *      either:
     *      FRESH (didn't pick any cards yet) 
     *      ONE_CARD_VALID (picked one card and ready to pick second)
     *      ONE_CARD_INVALID (picked 2nd card but according to the game rules
     *                        the card picked wasn't allowed)
     *      TWO_CARDS_MATCH (picked two similar cards) 
     *      TWO_CARDS_NO_MATCH (picked two cards which are not similar).
     *      
     *      The player's score in the game is score and the players currently played cards
     *      are represented by cards. Played cards are cards which the player either picked
     *      and are locked by him, or cards which he picked (in the case of TWO_CARDS_NO_MATCH,
     *      and ONE_CARD_INVALID only) and do not match and are waiting for release
     *      and remove from boards, or to check if they can
     *      go back to being face down. 
     *      
     *      attemptedToFlip represents whether the player has tried to flip a card
     * 
     * RI:
     *      score >= 0
     *      state in {FRESH, ONE_CARD_VALID, ONE_CARD_INVALID, TWO_CARDS_MATCH, TWO_CARDS_NO_MATCH}
     *      cards.size() <= 2
     *      playerId != null
     *      if state is ONE_CARD_VALID\INVALID:
     *          cards.size() == 1
     *      if state is FRESH:
     *          cards.size == 0
     *      if state is TWO_CARDS_MATCH or TWO_CARDS_NO_MATCH:
     *          cards.size == 2
     * 
     * Safety From Rep Exposure:
     *      playerID is final and cannot be mutated.
     *      The state changed according to game rules, in a threadsafe manner.
     *      Player score can only be mutated through the fixed game rules and it is
     *      not exposed to the client
     *      cards can only be changed through the game rules in a threadsaftey way 
     *      All fields are private and client cannot access them
     *      All changes to none final variables are done according to the RI.
     *      
     * Thread Safety Argument:
     *      All operations done on mutable variables of Player are done in a 
     *      synchronized thread safe manner. The changes made to Player are made through
     *      Serializeable lock acquisition.
     * 
     */
    
    
    /*
     * checks that our rep invariant is conserved
     */
    private void checkRep() {
        assert score >= 0;
        assert Set.of(PlayerState.FRESH, 
                PlayerState.ONE_CARD_VALID,
                PlayerState.ONE_CARD_INVALID,
                PlayerState.TWO_CARDS_MATCH,
                PlayerState.TWO_CARDS_NO_MATCH).contains(state);
        assert playerId != null;
        if (state.equals(PlayerState.FRESH)) {
            assert cards.isEmpty();
        }
        else if (state.equals(PlayerState.ONE_CARD_VALID) ||
                state.equals(PlayerState.ONE_CARD_INVALID)) {
            assert cards.size() == 1;
        }
        else {
            assert cards.size() == 2;
        }
    }
    
    /**
     * Constructor for Player ADT
     * @param id player ID
     */
    public Player (String id) {
        playerId = id;
        checkRep();
    }
    
    /**
     * Will result marking the player as attempted to flip a card
     */
    public void markAttempted() {
        attemptedToFlip = true;
    }
    
    /**
     * @return Will return whether the player attempted to flip a card
     */
    public boolean getAttempted() {
        return attemptedToFlip;
    }
    
    /**
     * 
     * @return the current player score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Adds one to current score
     */
    public void incrementScore() {
        score++;
        checkRep();
    }
    
    /**
     * 
     * @return player id
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /*
     * Two objects are equal if they share the same player id
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof Player) {
            return sameValue((Player) that);
        }
        return false;
    }
    
    /*
     * method to check if two Player objects have same player id
     */
    private boolean sameValue(Player that) {
        return getPlayerId().equals(that.getPlayerId());
    }
    
    
    @Override
    public int hashCode() {
        return playerId.hashCode();
    }
    
    /**
     * 
     * @param card the card the player just acquired 
     * @return true if managed to add it
     */
    public boolean takeCard(Card card) {
        cards.add(card);
        card.setOwner(this);
        return true;
    }
    
    /**
     * 
     * @return the state the player is currently in
     */
    public PlayerState getState() {
        checkRep();
        if (state == PlayerState.FRESH) {
            return PlayerState.FRESH;
        }
        else if (state == PlayerState.ONE_CARD_VALID) {
            return PlayerState.ONE_CARD_VALID;
        }
        else if (state == PlayerState.TWO_CARDS_MATCH) {
        return PlayerState.TWO_CARDS_MATCH;
        }
        else if (state == PlayerState.ONE_CARD_INVALID) {
            return PlayerState.ONE_CARD_INVALID;
        }
        return PlayerState.TWO_CARDS_NO_MATCH;
        
    }
    
    /**
     * 
     * @param newState set the new state of the player to newState
     */
    public void setState(PlayerState newState) {
        state = newState;
        checkRep();
    }
    
    /**
     * Return the string rep of player ID
     */
    @Override
    public String toString() {
        return playerId;
    }
    
    /**
     * 
     * @return the number of cards player currently ownes
     */
    public int numberOwnedCards() {
        return cards.size();
    }
    
    /**
     * Player must have two cards
     * @return will sort the cards based on their card number
     *         where the lower card number will be in position 0
     */
    public List<Card> getCards() {
        assert numberOwnedCards()==2;
        Card card1 = cards.get(0);
        Card card2 = cards.get(1);
        List<Card> orderedCards = new ArrayList<Card>();
        if (card1.getCardNumber() < card2.getCardNumber()) {
            orderedCards.add(card1);
            orderedCards.add(card2);
        }
        else {
            orderedCards.add(card2);
            orderedCards.add(card1);
        }
        return orderedCards;
    }
    
    
    /**
     * Will release all the cards the player has
     */
    public void releaseCards() {
        for (Card card : cards) {
            card.releaseCard();
        }
    }
    
    /**
     * Will empty the cards the player has
     */
    public void resetCardList() {
        cards = new ArrayList<Card>();
    }
    
    /**
     * 
     * @return The first card in the cards list
     */
    public Card getFirstCard() {
        assert cards.size() > 0;
        return cards.get(0);
    }
    
    /**
     * 
     * @return the score string according to the web server grammar config:
     *      "[PlayerId] [Score]\n"
     */
    public String webScoreString() {
        return playerId + " " + score;
    }
    
    
    
    
    
    
    
    
    
    

}

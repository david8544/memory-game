package memory;

/**
 *  Card class to represent card on board
 *  Card will hold string representation of the card
 * @author davidmagrefty
 *
 */
public class Card {
    
     private final String card;
     private final int cardNumber;
     private Player cardOwner;
     private boolean faceUp;
     private boolean isOwned;
     private boolean isRemoved;
    
    // Abstraction function:
    //   AF(card, cardNumber, cardOwner, faceUp, isOwned, isRemoved) = 
    //          The card on board which has value card and it has cardNumber based
    //          on the order of creating the board (ie. first card on board has card number
    //          one, and etc. The counting starts from rows. The owner of the card is card
    //          Owner and if the card is faceUp, faceUp will be true, otherwise false.
    //          If  the card is owned, isOwned will be true, otherwise false.
    //          If the card is removed from board, isRemoved will be true, otherwise false.
    // Representation invariant:
    //   All cards must have card and cardNumber which are not null.
    //   If card is Owned, card must be faceUp.
    //   If card is Removed, it cannot be owned.
    //   If isOwned is true, cardOwner must be different than null
    //   If faceUp is false, isOwned is false
    //
    // SRE:
    //   Procedure of changing card variables is done only by following the rep invariant
    //   After each change, we are making sure the rep is maintained.
    //   All Card parameters are primitives or immutable, except cardOwner. cardOwner is
    //   exposed, but handled only through board and maintains the rep invariant.
    // Thread safety argument:
    //   Card, all of the methods and operations that handle Card are threadsafe. The operations
     //  work in a threadsafe serialized manner by acquiring locks (for mutation) in a
     //  serialized manner. Therefore, Card is a threadsafe datatype as it is only handled
     // by following a threadsafe manner by the different datatypes that operate on it.
     
    
    public Card(String cardInput, int cardNum) {
        card = cardInput;
        cardOwner = null;
        faceUp = false;
        isOwned = false;
        isRemoved = false;
        cardNumber = cardNum;
        checkRep();
    }
    
    
    public Card(String cardValue, 
            Player owner, 
            boolean face, 
            boolean locked, 
            boolean removed,
            int cardNum) {
        card = cardValue;
        cardOwner = owner;
        faceUp = face;
        isOwned = locked;
        isRemoved = removed;
        cardNumber = cardNum;
        checkRep();
    }
    
    /*
     * Checks our rep invariant is conserved
     */
    private void checkRep() {
        //   All cards must have card and cardNumber which are not null.
        //   If card is Owned, card must be faceUp.
        //   If card is Removed, it cannot be owned.
        //   If isOwned is true, cardOwner must be different than null
        //   If faceUp is false, isOwned is false
        assert card != null;
        if (!faceUp || isRemoved) {
            assert !isOwned;
            assert cardOwner == null;
        }
        if(isOwned) {
            assert cardOwner != null;
            assert faceUp;
        }

    }
    
    /*
     * returns the string rep of card
     */
    @Override
    public String toString() {
        return card;
    }
    
    /**
     * Create Card duplicate
     * @return duplicate of Card
     */
    public Card duplicate() {
        return new Card(card, cardOwner, faceUp, isOwned, isRemoved, cardNumber);
    }
    
    /**
     * 
     * @return whether the card is facing up or not
     */
    public boolean isFacingUp() {
        return faceUp;
    }
    
    /**
     * 
     * @return whether the card is removed from board
     */
    public boolean isRemoved() {
        return isRemoved;
    }
    
    /**
     * 
     * @return player who is currently locking the card
     */
    public Player getCardOwner() {
        return cardOwner;
    }
    
    /**
     * 
     * @return whether the card is locked by other player
     */
    public boolean isOwned() {
        return isOwned;
    }
    
    /**
     * releases the card from its current owner
     * Card must be owned by player
     */
    public void releaseCard() {
        assert isOwned;
        assert cardOwner != null;
        isOwned = false;
        cardOwner = null;
        checkRep();
    }
    
    /**
     * Card must not be owned by other players
     * @param p player to acquire the card
     */
    public void setOwner(Player p) {
        assert !isOwned;
        assert cardOwner == null;
        cardOwner = p;
        isOwned = true;
        checkRep();
    }
    
    /**
     * will set the card to be removed
     * Card must not be owned by player
     */
    public void removeCard() {
        isRemoved = true;
        assert !isOwned;
        assert cardOwner == null;
        checkRep();
    }
    
    /**
     * Will change the orientation of the card to up
     * Card must be face down
     */
    public void flipCardUp() {
        assert !faceUp;
        faceUp = true;
    }
    
    /**
     * Will change the orientation of the card to down
     * Card must be face up
     */
    public void flipCardDown() {
        assert faceUp;
        faceUp = false;
    }
    
    /**
     * 
     * @return the card number associated with the card
     */
    public int getCardNumber() {
        return cardNumber;
    }
    
    /**
     * 
     * @return the string representing this card
     */
    public String getCardValue() {
        return card;
    }
    
    
    

}

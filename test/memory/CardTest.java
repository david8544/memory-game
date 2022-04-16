package memory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

// Testing class for Card ADT
public class CardTest {
    
    /**
     * Testing Strategy:
     * 
     *    Constructor:
     *          Input: cardValue, cardNumber
     *                 From duplicate
     *    toString()
     *          Input: true
     *          Output: Same as card value
     *    
     *    duplicate():
     *          Input: this
     *          Output: true
     *          
     *    isFacingUp():
 *              Input: card face up, card face down
 *              Output: true
 *              
 *        isRemoved():
 *              Input: card removed, card not removed
 *              Output: true
 *              
 *        getCardOwner():
 *              Input: null, player
 *              Output: true
 *              
 *        releaseCard():
 *              Input: card is owned
 *              Output: card is released
 *        
 *        removeCard():
 *              Input: not removed
 *              Output: removed
 *              
 *        flipCardUp():
 *              Input: card is down
 *              Output: card is up
 *        
 *        flipCardDown():
 *              Input: card is up
 *              Output: card is down
 *        
 *        setOwner(Player):
 *              Input: card is not owned
 *              Output: card is Owned by player
 *        
 *        
     */
    
    /*
     * Covers the following partitions:
     *    Constructor:
     *          Input: cardValue, cardNumber
     *    toString()
     *          Input: true
     *          Output: Same as card value
     *          
     *    isFacingUp():
 *              Input: card face up, card face down
 *              Output: true
 *              
 *        isRemoved():
 *              Input: card removed, card not removed
 *              Output: true
 *              
 *        releaseCard():
 *              Input: card is owned
 *              Output: card is released
 *        
 *        removeCard():
 *              Input: not removed
 *              Output: removed
 *              
 *        flipCardUp():
 *              Input: card is down
 *              Output: card is up
 *        
     */
    @Test
    public void testCardMulOper() {
        Player p = new Player("a");
        Card card = new Card("b", 1);
        assertFalse(card.isRemoved(), "Card should not be removed");
        assertFalse(card.isOwned(), "Card should not be owned");
        assertFalse(card.isFacingUp(), "Card should not be facing up");
        assertEquals("b", card.toString(),"card should have same string as its value");
        card.flipCardUp();
        assertTrue(card.isFacingUp(), "card should be facing up");
        card.setOwner(p);
        assertTrue(card.isOwned(), "card should be owned");
        card.releaseCard();
        card.removeCard();
        assertTrue(card.isRemoved(),"card should be removed");
        
        
    }
    
    
    /*
     * Constructor:
     *          Input: cardValue, cardNumber
     *                 From duplicate
     *    
     *    duplicate():
     *          Input: this
     *          Output: true
     *          
     *    isFacingUp():
 *              Input: card face up, card face down
 *              Output: true
 *              
 *        getCardOwner():
 *              Input: null, player
 *              Output: true
 *              
 *        releaseCard():
 *              Input: card is owned
 *              Output: card is released
 *              
 *        flipCardUp():
 *              Input: card is down
 *              Output: card is up
 *        
 *        flipCardDown():
 *              Input: card is up
 *              Output: card is down
 *        
 *        setOwner(Player):
 *              Input: card is not owned
 *              Output: card is Owned by player
 *        
     */
    
    @Test
    public void testCardPlayerOper() {
        Player p = new Player("a");
        Card card = new Card("b", 1);
        assertEquals(null, card.getCardOwner(),"should not have owner");
        card.flipCardUp();
        card.setOwner(p);
        assertEquals(p, card.getCardOwner(), "p should be owner");
        card.releaseCard();
        card.flipCardDown();
        assertFalse(card.isFacingUp(), "Card should be face up");
        Card cardDup = card.duplicate();
        assertEquals(cardDup.getCardNumber(), card.getCardNumber(), "dup cards should have same number");
    }

}

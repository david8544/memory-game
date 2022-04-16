package memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

// Test Class for the Player ADT
public class PlayerTest {
    
    /*
     * Testing Strategy:
     * 
     *      Testing partitions for markAttempted():
     *          Input: this.attempted = true, false
     *          Output: true
     *      
     *      Testing partitions for getAttempted():
     *          Input: this.attempted = true, false
     *          Output: true, false
     *      
     *      Testing partitions for getScore():
     *          Input: true
     *          Output: true
     *      
     *      Testing partitions incrementScore():
     *          Input: true
     *          Output: score incremented
     *      
     *      Testing partition for getPlayerId():
     *          Input: true
     *          Output: playerID
     *      
     *      Testing partitions for equals():
     *          Input: this==that, this != that
     *          Output: true, false
     *      
     *      Testing partitions for takeCard():
     *          Input: this.cards.size() =0, =1
     *          Output: true
     *      
     *      Testing partitions for getCards():
     *          Input: this.cards.size() == 2
     *          Output: cards[0].number < cards[1].number
     *      
     *      Testing partitions for releaseCards():
     *          Input: this.cards.size() =0, <0
     *          Output: true
     *      
     *      Testing partitions for webScoreString():
     *          Input: true
     *          Output: matches grammar
     *      
     */
    
    /*
     *  The test covers the following partitions:
     *      
     *      Testing partition for getPlayerId():
     *          Input: true
     *          Output: playerID
     *          
     *      Testing partitions for equals():
     *          Input: this != that
     *          Output: false    
     */
    
    private static final Card C1 = new Card("a", 0);
    private static final Card C2 = new Card("b", 1);
    
    
    @Test 
    public void testEqualsAndString() {
        Player p = new Player("ab");
        Player p1 = new Player("ac");
        assertNotEquals(p,p1, "Players with different IDs are not equals");
        assertEquals("ab", p.getPlayerId(), "Should return player id");
        assertEquals("ab", p.toString(), "ToString should be same as player id");
        
    }
    
    /*
     *  The test covers the following partitions:
     *  
     *      Testing partitions for markAttempted():
     *          Input: this.attempted = true, false
     *          Output: true
     *      
     *      Testing partitions for getAttempted():
     *          Input: this.attempted = true, false
     *          Output: true, false
     *      
     *      Testing partition for getPlayerId():
     *          Input: true
     *          Output: playerID
     *      
     *      Testing partitions for equals():
     *          Input: this==that
     *          Output: true
     *      
     *      Testing partitions for takeCard():
     *          Input: this.cards.size() =0, =1
     *          Output: true
     *      
     *      Testing partitions for getCards():
     *          Input: this.cards.size() == 2
     *          Output: cards[0].number < cards[1].number
     *      
     *      Testing partitions for releaseCards():
     *          Input: this.cards.size() >0
     *          Output: true
     *      
     *      Testing partitions for webScoreString():
     *          Input: true
     *          Output: matches grammar
     */
    
    
    @Test 
    public void testMultiOperPlayer() {
        C1.flipCardUp();
        C2.flipCardUp();
        Player p = new Player("ab");
        assertFalse(p.getAttempted(),"did not attempt yet");
        p.markAttempted();
        assertTrue(p.getAttempted(), "p should be marked");
        p.incrementScore();
        assertEquals(1, p.getScore(), "only one valid");
        assertEquals("ab 1", p.webScoreString(), "web string should match grammar");
        p.takeCard(C2);
        p.setState(PlayerState.ONE_CARD_VALID);
        assertEquals(1, p.numberOwnedCards(), "should only own one card");
        p.takeCard(C1);
        p.setState(PlayerState.TWO_CARDS_MATCH);
        assertEquals(2, p.numberOwnedCards(), "should only own two card");
        List<Card> cards = p.getCards();
        assertEquals(2, cards.size(), "should have two cards");
        assertTrue(cards.get(0).getCardNumber() < cards.get(1).getCardNumber(), 
                "cards should return in small to large order");
        p.releaseCards();
        p.resetCardList();
        assertEquals(0, p.numberOwnedCards(), "should have one cards");
        Player p1 = new Player("ab");
        assertEquals(p1, p, "Players with same ID should equals");
        assertEquals(p1.hashCode(), p.hashCode(), "Hash code of equal objects is the same");
        
    }

}

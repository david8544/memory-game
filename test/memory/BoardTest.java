/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import java.util.List;



/**
Testing class for Board
 */
public class BoardTest {
    
    // Testing strategy
    /*
     * Testing for parseFromFile(String filename):
     *      Input:
     *          Partition on filename: 
     *              exist, does not exist (throws error)
     *          Partition on board size:
     *              row.size() =1, >1
     *              col.size() =1, >1
     *      Output:
     *         throws error, 
     *         returns valid board (input col == output col, same for row) 
     *         
     *  
     *  Testing for generateRandom(int rows, int columns, Set<String> cards):
     *      Input:
     *          Partition on rows: =1, >1
     *          Partition on cols: =1, >1
     *          Partition on cards.size(): =1, >1
     *          Partition on card room on board: 
     *                              perfect match: (rows*cols) // cards.size() == 2,
     *                              more cards than places to put cards (ie. rows*cols =1,
     *                                                                     cards.size()=2)
     *                                                                     
     *                              
     *     Output:
     *          returns valid board probability distribution   
     *  
     *  Testing strategy for toString():
     *      Input:
     *          Partition on board structure: 
     *              Board rows == cols, Board rows != cols
     *      Output:
     *          Board parsed as spec 
     *          
     *  Testing strategy for flip(player, row, col):
     *      Input: 
     *          player controls cards.size(): =0, =1, =2
     *          Testing all rules: 1A, 1B, 1C, 1D, 2A, 2B, 2C, 2D, 2E, 3A, 3B
     *          Players in game: 1, >1
     *      
     *      Output:
     *          Card Match: Players increments point, Player still holds card
     *          Cards don't match: Cards don't match, score does not increment, player does not hold card
     *          cards stay up
     *          Card blocked
     *          Player gets card 
     *      
     * 
     */
    private static final String BOARD_DIRECTORY = "boards/";
    
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 2
     *          player controls 0 cards, 1 card, 2 cards
     *          rule: 2A
     *          Card is removed
     */
    @Test
    public void testTwoPlayers2A() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Player p2 = new Player("2");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        // player 1 takes two matching cards
        board.flipCard(p1, 0, 0);
        board.flipCard(p1, 0, 1);
        Card card1p1 = p1.getFirstCard();
        Card card2p1 = p1.getCards().get(1);
        // player 2 takes first card
        board.flipCard(p2, 0, 2);
        
        // player 1 takes next card
        board.flipCard(p1, 1, 1);
        assertTrue(card1p1.isRemoved(), "card should be removed");
        assertTrue(card2p1.isRemoved(), "card should be removed");
        
        // player 2 tries to take removed card
        board.flipCard(p2, 0, 0);
        Card card1p2 = p2.getFirstCard();
        assertEquals(PlayerState.ONE_CARD_INVALID, p2.getState(), "state should be invalid");
        assertEquals(null, card1p2.getCardOwner(), "should not be owned");
        assertTrue(card1p2.isFacingUp(), "Card should still face up");
        System.out.println(board.webString(p2));
        
    }
    
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 2
     *          player controls 0 cards, 1 card, 2 cards
     *          rule: 1C
     *          Player gets card
     */
    @Test
    public void testTwoPlayers1C() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Player p2 = new Player("2");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        board.flipCard(p1, 0, 0);
        board.flipCard(p1, 0, 2);
        board.flipCard(p2, 0, 0);
        Card card1p1 = p1.getFirstCard();
        Card card2p1 = p1.getCards().get(1);
        assertTrue(card1p1.isFacingUp(), "Valid move card should face up");
        assertEquals(p2, card1p1.getCardOwner(), "P1 should own the card after valid move");
        assertEquals(1, p2.numberOwnedCards(), "P2 should not own any cards");
        assertEquals(null, card2p1.getCardOwner(), "Card should not be ownes");
        assertTrue(card2p1.isFacingUp(), "Card should stay face up");
        
    }
    
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 2
     *          player controls 0 cards, 1 card, 2 cards
     *          rule: 1B, 1C, 2B, 2E
     *          Player gets card
     */
    @Test
    public void testTwoPlayerFight() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Player p2 = new Player("2");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        board.flipCard(p1, 0, 0);
        board.flipCard(p2, 0, 2);
        Card card1p1 = p1.getFirstCard();
        Card card1p2 = p2.getFirstCard();
        assertTrue(card1p1.isFacingUp(), "Valid move card should face up");
        assertTrue(card1p2.isFacingUp(), "Valid move card should face up");
        assertEquals(p1, card1p1.getCardOwner(), "P1 should own the card after valid move");
        assertEquals(p2, card1p2.getCardOwner(), "P2 should own the card after valid move");
        // test 2B
        board.flipCard(p2, 0, 0);
        assertTrue(card1p1.isFacingUp(), "after 2b cards should stay up");
        assertTrue(card1p2.isFacingUp(), "after 2b cards should stay up");
        assertEquals(PlayerState.ONE_CARD_INVALID, p2.getState());
        assertFalse(card1p2.isOwned(), "Card should not be owned");
        assertEquals(null, card1p2.getCardOwner());
        // test 1C + 2E
        board.flipCard(p1, 0, 2);
        assertTrue(card1p2.isFacingUp(), "Card taken should face up");
        assertTrue(card1p1.isFacingUp(), "Card should be face up");
        assertEquals(null, card1p2.getCardOwner(), "card should not be owned");
        assertEquals(null, card1p1.getCardOwner(), "card should not be owned");
        assertEquals(PlayerState.TWO_CARDS_NO_MATCH, p1.getState(), "player should be with two cards");    
    }
    
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 1
     *          player controls 0 cards, 1 card, 2 cards
     *          rule: 1B, 2B
     *          Player gets card
     */
    @Test
    public void testPlayerFailure2ndCard() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        board.flipCard(p1, 0, 0);
        board.flipCard(p1, 0, 0);
        assertEquals(0, p1.getScore(), "Score should not increment");
        Card card1 = p1.getFirstCard();
        assertEquals(PlayerState.ONE_CARD_INVALID, p1.getState(), "Player should be in an invalid");
        assertTrue(card1.isFacingUp(), "After wrong second move card should still face up");
        assertFalse(card1.isOwned(), "Card should not be owned after failure second move");
        assertEquals(1, p1.numberOwnedCards(),"should only have the first card");
        board.flipCard(p1, 2, 2);
        assertFalse(card1.isFacingUp(), "Card should return to face down");
        assertEquals(1, p1.numberOwnedCards(), "Player should only hold one card");
        assertEquals(p1, p1.getFirstCard().getCardOwner(), "Player should own card");
    }
    
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 1
     *          player controls 0 cards
     *          rule: 1B
     *          Player gets card
     */
    @Test
    public void testPlayerFirstMove() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        board.flipCard(p1, 0, 0);
        Card cardFlipped = p1.getFirstCard();
        assertEquals(p1, cardFlipped.getCardOwner(), "After valid flip player controls cards");
        assertTrue(cardFlipped.isFacingUp(), "After flip card is facing up");

    }
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 1
     *          player controls 0 cards, 1 card, 2 cards
     *          rule: 1A, 1B, 2C, 2D, 3A
     *          Player gets card
     */
    @Test
    public void testPlayerCompleteMoveMatch() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        board.flipCard(p1, 0, 0);
        board.flipCard(p1, 0, 1);
        assertEquals(1, p1.getScore());
        List<Card> cards = p1.getCards();
        Card card1 = cards.get(0);
        Card card2 = cards.get(1);
        assertTrue(card1.isFacingUp(), "After match cards supposed to face up");
        assertTrue(card2.isFacingUp(), "After match cards supposed to face up");
        assertEquals(p1, card1.getCardOwner(), "After match players still control cards");
        assertEquals(p1, card2.getCardOwner(), "After match players still control cards");
        assertFalse(card1.isRemoved(), "Card should not be removed before player next play");
        assertFalse(card2.isRemoved(), "Card should not be removed before player next play");
        board.flipCard(p1, 0, 0);
        assertEquals(0, p1.numberOwnedCards(), "After next move matched cards are not owned by player");
        assertTrue(card1.isRemoved(), "After next move cards should be removed");
        assertTrue(card1.isRemoved(),"After next move cards should be removed");
    }
    
    /*
     * Test covers following partitions:
     *      flip:
     *          player in game = 1
     *          player controls 0 cards, 1 card, 2 cards
     *          rule: 1B, 2C, 2E, 3B
     *          Player gets card
     */
    @Test
    public void testPlayerCompleteMoveDontMatch() throws InterruptedException, IOException {
        Player p1 = new Player("1");
        Board board = Board.parseFromFile(BOARD_DIRECTORY + "perfect.txt");
        board.flipCard(p1, 0, 0);
        board.flipCard(p1, 0, 2);
        assertEquals(0, p1.getScore());
        List<Card> cards = p1.getCards();
        Card card1 = cards.get(0);
        Card card2 = cards.get(1);
        assertTrue(card1.isFacingUp(), "After no match cards supposed to face up");
        assertTrue(card2.isFacingUp(), "After no match cards supposed to face up");
        assertFalse(card1.isOwned(), "Card should not be owned after mismatch");
        assertFalse(card2.isOwned(), "Card should not be owned after mismatch");
        assertFalse(card1.isRemoved(), "Card should not be removed no match");
        assertFalse(card2.isRemoved(), "Card should not be removed no match");
        assertEquals(PlayerState.TWO_CARDS_NO_MATCH, p1.getState(), "should be in mismatch state");
        assertEquals(2, p1.numberOwnedCards(), "Should still have both cards");
        board.flipCard(p1, 0, 0);
        assertEquals(1, p1.numberOwnedCards(), "After next move matched cards are not owned by player");
        assertTrue(card1.isFacingUp(), "After next move cards should be removed");
        
    }
    /*
     * Test covers following partitions:
     *      generateRandom:
     *          Partition on rows: =1
     *          Partition on cols: =1
     *          Partition on cards.size() = 1
     *          Partition card room: perfect
     */
    @Test
    public void testGenerateRandOneSquare() {
        final Set<String> cards = Set.of("A");
        final int rows = 1;
        final int cols = 1;
        final Board singleBoard = Board.generateRandom(rows, cols, cards);
        final String expected = "Board Size: 1x1\n" + "A\n";
        assertEquals(expected, singleBoard.toString());
    }
    
    /*
     * Test covers following partitions:
     *      generateRandom:
     *          Partition on rows: >1
     *          Partition on cols: >1
     *          Partition on cards.size() = 1
     *          Partition card room: perfect
     */
    @Test
    public void testGenerateRand2x2SquareOneCard() {
        final Set<String> cards = Set.of("A");
        final int rows = 2;
        final int cols = 2;
        final Board squareBoard = Board.generateRandom(rows, cols, cards);
        final String expected = "Board Size: 2x2\n" + "A A\n" + "A A\n";
        assertEquals(expected, squareBoard.toString());
    }
    
    /*
     * Test covers following partitions:
     *      generateRandom:
     *          Partition on rows: =1
     *          Partition on cols: =1
     *          Partition on cards.size() > 1
     *          Partition card room: not perfect
     */
    @Test
    public void testGenerateRandSquareThreeCards() {
        final Set<String> cards = Set.of("A", "B", "C");
        final int rows = 1;
        final int cols = 1;
        final Board squareBoard = Board.generateRandom(rows, cols, cards);
        final String possibleA = "Board Size: 1x1\n" + "A\n";
        final String possibleB = "Board Size: 1x1\n" + "B\n";
        final String possibleC = "Board Size: 1x1\n" + "C\n";
        assertTrue((squareBoard.toString().equals(possibleA) || 
                    squareBoard.toString().equals(possibleB) || 
                    squareBoard.toString().equals(possibleC)));
    }
    /*
     * Test covers following partitions:
     *      generateRandom:
     *          Partition on rows: >1
     *          Partition on cols: =1
     *          Partition on cards.size() > 1
     *          Partition card room: not perfect
     */
    @Test
    public void testGenerateRectBoardTwoCards() {
        final Set<String> cards = Set.of("A", "B");
        final int rows = 1;
        final int cols = 3;
        final Board squareBoard = Board.generateRandom(rows, cols, cards);
        final String possibleA1 = "Board Size: 1x3\n" + "A A B\n";
        final String possibleA2 = "Board Size: 1x3\n" + "A B A\n";
        final String possibleA3 = "Board Size: 1x3\n" + "B A A\n";
        final String possibleB1 = "Board Size: 1x3\n" + "B B A\n";
        final String possibleB2 = "Board Size: 1x3\n" + "B A B\n";
        final String possibleB3 = "Board Size: 1x3\n" + "A B B\n";
        assertTrue((squareBoard.toString().equals(possibleA1) || 
                    squareBoard.toString().equals(possibleA2) || 
                    squareBoard.toString().equals(possibleA3) ||
                    squareBoard.toString().equals(possibleB1) ||
                    squareBoard.toString().equals(possibleB2) ||
                    squareBoard.toString().equals(possibleB3)));
    }
    
    /*
     * Test covers following partitions:
     *      parseFromFile:
     *          Partition on filename: exist
     *          Partition on board size: row.size()=1, col.size()=1
     *      toString:
     *          Partition on Board Structure: rows==cols
     */
    @Test
    public void testParserOneSquareBoard() throws IOException{
        final String singleAFile = BOARD_DIRECTORY + "singleA.txt";
        final Board singleABoard = Board.parseFromFile(singleAFile);
        final String expected = "Board Size: 1x1\n" + "A\n";
        assertEquals(expected, singleABoard.toString());
    }
    
    /*
     * Test covers following partitions:
     *      parseFromFile:
     *          Partition on filename: exist
     *          Partition on board size: row.size()>1, col.size()>1
     *      toString:
     *          Partition on Board Structure: rows!=cols
     */
    @Test
    public void testRectangleABBoard() throws IOException{
        final String rectABFile = BOARD_DIRECTORY + "rectangleAB.txt";
        final Board singleABoard = Board.parseFromFile(rectABFile);
        final String expected = "Board Size: 2x3\n" + "A B A\n" + "B A B\n";
        assertEquals(expected, singleABoard.toString());
    }
    
    /*
     * Test covers following partitions:
     *      parseFromFile:
     *          Partition on filename: exist
     *          Partition on board size: row.size()>1, col.size()>1
     *      toString:
     *          Partition on Board Structure: rows==cols
     */
    @Test
    public void testParser2x2SquareBoard() throws IOException{
        final String squareAFile = BOARD_DIRECTORY + "squareA.txt";
        final Board singleABoard = Board.parseFromFile(squareAFile);
        final String expected = "Board Size: 2x2\n" + "A A\n" + "A A\n";
        assertEquals(expected, singleABoard.toString());
    }
    
    /*
     * Test covers following partitions:
     *      parseFromFile:
     *          Partition on filename: throws error
     *          Partition on board size: -
     */
    @Test
    public void testParserFileNotExist() throws IOException{
        final String notExist = BOARD_DIRECTORY + "squareAcheck.txt";
        assertThrows(IOException.class,() -> Board.parseFromFile(notExist));
    }
    
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    
}

/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
Testing Class for the WebServer
 */
public class WebServerTest {
    
    // Testing strategy
    /*
     *      Testing partitions for watch:
     *          Input:
     *              Number of Players: =1, >1
     *              Player is new, Player was already in game
     *              Board status: No change, Card flipped, Card removed
     *          Output:
     *              Send output correctly when there is a change
     * 
     * 
     *      Testing partitions for flip:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = Fresh, One_Card_Valid, One_card_invalid,
     *                             Two_cards_match, Two_cards_no_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0, >1
     *              Card to flip status: face down, face up locked,
     *                                   face up not locked, removed
     *         Output:
     *              Player takes card
     *              Player is blocked
     *              Player looses cards (chose blocked card or removed card for second card)
     *      
     * 
     *      Testing Partitions for look:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = Fresh, One_Card_Valid, One_card_invalid,
     *                             Two_cards_match, Two_cards_no_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0, >1
     *         Output:
     *              Valid string based on the grammar of the web server
     *              
     *              
     *      
     *      Testing Partitions for Score:
     *          Input:
     *              Number of players: 0, 1, >1
     *              Player attempted: true, false
     *              ** Mix between player number of player and if each attempted **
     *              Score value: =0, >0
     *          Output:
     *              Empty - no player attempted or 0 players
     *              Not empty - corresponds to web grammar
     *              
     *              
     * 
     * 
     */
    
    // Manual tests
    /*
     * 1. Run the game server.
     * 2. Go to http://web.mit.edu/6.031/www/sp20/psets/ps4/play/
     * 3. Enter localhost:8080
     *      Open incognito window and repeat 1-3
     * 4. Press play for both screens
     * 5. Play the game
     * 6. Make sure card removal, card blocking and card choose works.
     * 7. Make sure score is incremented correctly.
     * 8. Repeat and test all rules 1A - 3C
     */
   
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
//    @Test
//    public void testNotValidPlayerId() throws IOException {
//        Board board = makeBoard();
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        final URL lookError = new URL("http://localhost:" + 
//                server.port() + "/look/!");
//        final String stringError = parseResultedString(server, lookError);
//        final String expectedLookError = "Player ID does not follow the requirements";
//        assertEquals(expectedLookError, stringError, "should raise error");
//    }
    /*
     *      Testing partitions for watch:
     *          Input:
     *              Number of Players: =1, >1
     *              Player is new, Player was already in game
     *              Board status: No change, Card flipped, Card removed
     *          Output:
     *              Send output correctly when there is a change
     */
    @Test
    public void testWatchTwoCardsFlip() throws IOException{
        Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        final URL watch = new URL("http://localhost:" + 
                server.port() + "/watch/p2");
        final URL p1Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,1");
        final URL p1Flip12A = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,2");
        parseResultedString(server, p1Flip11A);
        parseResultedString(server, p1Flip12A);
        final InputStream watchInputStream = watch.openStream();
        final BufferedReader watchInput = new BufferedReader(new InputStreamReader(watchInputStream, UTF_8));
        final URL p1Flip21B = new URL("http://localhost:" + 
                server.port() + "/flip/p1/2,1");
        parseResultedString(server, p1Flip21B);
        String watchResponse = "";
        String line = watchInput.readLine();
        while (line != null) {
            watchResponse += line + "\n";
            line = watchInput.readLine();
        }
        final String expectedWatch = "3x3\n" + 
                "down\n" + 
                "up B\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedWatch, watchResponse);
        parseResultedString(server, p1Flip11A);
        assertEquals(null, watchInput.readLine());
        server.stop();
        
    }
    /*
     *      Testing partitions for watch:
     *          Input:
     *              Number of Players: =1
     *              Player was already in game
     *              Board status: No change, Card flipped
     *          Output:
     *              Send output correctly when there is a change
     *              
     */
    @Test
    public void testWatchCardFlipOnlyOnce() throws IOException{
        Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        final URL watch = new URL("http://localhost:" + 
                server.port() + "/watch/p1");
        final URL p1Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,1");
        final InputStream watchInputStream = watch.openStream();
        final BufferedReader watchInput = new BufferedReader(new InputStreamReader(watchInputStream, UTF_8));
        parseResultedString(server, p1Flip11A);
        String watchResponse = "";
        String line = watchInput.readLine();
        while (line != null) {
            watchResponse += line + "\n";
            line = watchInput.readLine();
        }
        final String expectedWatch = "3x3\n" + 
                "my A\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedWatch, watchResponse);
        parseResultedString(server, p1Flip11A);
        assertEquals(null, watchInput.readLine());
        server.stop();
        
    }
    /*
     * 
     * 
     *      Testing partitions for flip:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = Fresh, One_Card_Valid, One_card_invalid,
     *                             Two_cards_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0, >1
     *              Card to flip status: face down, face up locked,
     *                                   removed
     *         Output:
     *              Player takes card
     *              Player is blocked
     *              Player looses cards (chose blocked card or removed card for second card)
     *      
     * 
     *      Testing Partitions for look:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = One_card_invalid,
     *                             Two_cards_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0, >1
     *         Output:
     *              Valid string based on the grammar of the web server
     *              
     *              
     */
    @Test
    public void testBlockingAndNotWinningCard() throws IOException {
        Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        // p1 takes 1,1 A
        final URL p1Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,1");
        parseResultedString(server, p1Flip11A);
        // p2 tried to take locked card 1,1 A
        final URL p2Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p2/1,1");
        p2Flip11A.openStream();
        // p1 takes second card match 1,3 A
        final URL p1Flip13A = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,3");
        parseResultedString(server, p1Flip13A);
        // p2 is still locked, trying to take another card 2,1 B
        final URL p2Flip13A = new URL("http://localhost:" + 
                server.port() + "/flip/p2/1,3");
        p2Flip13A.openStream();
        final URL p2Look = new URL("http://localhost:" +  
                server.port() + "/look/p2");
        final String actualP2LookString = parseResultedString(server, p2Look);
        final String expectedP2LookString = "3x3\n" + 
                "up A\n" + 
                "down\n" + 
                "up A\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedP2LookString, actualP2LookString, "should not have any cards");
        // p1 tries to take a removed card, 
        final URL p1Flip13BAgain = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,3");
        final String actualBoardString = parseResultedString(server, p1Flip13BAgain);
        final String expectedBoardString = "3x3\n" + 
                "none\n" + 
                "down\n" + 
                "none\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedBoardString, actualBoardString);
        server.stop();
        
        
    }
    /*
     * 
     * 
     *      Testing partitions for flip:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = Fresh, One_Card_Valid, One_card_invalid,
     *                             Two_cards_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0, >1
     *              Card to flip status: face up locked,
     *                                   removed
     *         Output:
     *              Player takes card
     *              Player is blocked
     *              Player looses cards (chose blocked card or removed card for second card)
     *              
     *              
     *      
     *      Testing Partitions for Score:
     *          Input:
     *              Number of players: 0, 1, >1
     *              Player attempted: true, false
     *              ** Mix between player number of player and if each attempted **
     *              Score value: =0, >0
     *          Output:
     *              Empty - no player attempted or 0 players
     *              Not empty - corresponds to web grammar
     *              
     *              
     * 
     */
    @Test
    public void testBlockingAndWinningCard() throws IOException {
        Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        final URL p1Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p1/1,1");
        parseResultedString(server, p1Flip11A);
        final URL p2Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p2/1,1");
        p2Flip11A.openStream();
        final URL p1Flip21B = new URL("http://localhost:" + 
                server.port() + "/flip/p1/2,1");
        parseResultedString(server, p1Flip21B);
        final URL p2Flip13A = new URL("http://localhost:" + 
                server.port() + "/flip/p2/1,3");
        final String actualBoardString = parseResultedString(server, p2Flip13A);
        final String expectedBoardString = "3x3\n" + 
                "my A\n" + 
                "down\n" + 
                "my A\n" + 
                "up B\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedBoardString, actualBoardString);
        
        server.stop();
    }
    
    /*
     *      Testing partitions for flip:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = Fresh, One_Card_Valid
     *                             Two_cards_no_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0
     *              Card to flip status: face down, 
     *                                   face up not locked
     *         Output:
     *              Player takes card
     *              Player looses cards (chose blocked card or removed card for second card)
     */
    @Test
    public void testTwoCardsNoMatchScoreBoard() throws IOException {
        Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        // p1 flips first card A
        final URL p1Flip11A = new URL("http://localhost:" + 
        server.port() + "/flip/p1/1,1");
        parseResultedString(server, p1Flip11A);
        final URL p1Flip12B = new URL("http://localhost:" + 
        server.port() + "/flip/p1/1,2");
        final String actualTwoFlipsNoMatch = 
                parseResultedString(server, p1Flip12B);
        final String expectedTwoFlipNoMatch = "3x3\n" + 
                "up A\n" + 
                "up B\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedTwoFlipNoMatch, 
                actualTwoFlipsNoMatch, "should not hold cards");
        final URL lookP2 = new URL("http://localhost:" + 
                server.port() + "/look/p2");
        final String lookP2Actual = parseResultedString(server, lookP2);
        assertEquals(expectedTwoFlipNoMatch, lookP2Actual, "Should be the same as if not played");
        final URL score = new URL("http://localhost:" + server.port() + "/scores"); 
        final String scoreNoMatch = parseResultedString(server, score);
        assertEquals("p1 0\n", scoreNoMatch, "should only have one player");
        final URL p2Flip11A = new URL("http://localhost:" + 
                server.port() + "/flip/p2/1,1");
        final String actualP2Flip11AString = parseResultedString(server, p2Flip11A);
        final String expectedP2Flip11AString = "3x3\n" + 
                "my A\n" + 
                "up B\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedP2Flip11AString, actualP2Flip11AString, "should only have one card up");
        
        server.stop();
        
        
    }
    
    /*
     * 
     *      Testing partitions for flip:
     *          Input:
     *              Number of players: =1, >1
     *              Player.state = Fresh, One_Card_Valid, One_card_invalid,
     *                             Two_cards_match
     *              Number of cards owned by other players: =0, =1, >1
     *              Number of removed cards: =0, >1
     *              Card to flip status: face down, face up locked,
     *                                   
     *         Output:
     *              Player takes card
     *              Player looses cards (chose blocked card or removed card for second card)
     *      
     * 
     *      Testing Partitions for look:
     *          Input:
     *              Number of players: =>1
     *              Player.state = Fresh, One_Card_Valid, One_card_invalid,
     *                             Two_cards_match
     *              Number of cards owned by other players: >1
     *              Number of removed cards: >1
     *         Output:
     *              Valid string based on the grammar of the web server
     *              
     *              
     *      
     *      Testing Partitions for Score:
     *          Input:
     *              Number of players: 0, 1, >1
     *              Player attempted: true, false
     *              ** Mix between player number of player and if each attempted **
     *              Score value: =0, >0
     *          Output:
     *              Empty - no player attempted or 0 players
     *              Not empty - corresponds to web grammar
     *              
     *              
     * 
     * 
     */
    @Test
    public void testTwoPlayersWithRemovedCards() throws IOException{
        Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        // p1 flips first card A
        final URL p1Flip11A = new URL("http://localhost:" + server.port() + "/flip/p1/1,1");
        parseResultedString(server, p1Flip11A);
        final URL p2FlipLook = new URL("http://localhost:" + server.port() + "/look/p2");
        parseResultedString(server, p2FlipLook);
        final URL score = new URL("http://localhost:" + server.port() + "/scores"); 
        final String scoreWoP2 = parseResultedString(server, score);
        assertEquals("p1 0\n", scoreWoP2, "should not include p2 since not attempted");
        // p2 flips first card B
        final URL p2Flip12B = new URL("http://localhost:" + server.port() + "/flip/p2/1,2");
        final String actualFlip12String = parseResultedString(server, p2Flip12B);
        final String expectedFlip12String = "3x3\n" + 
                "up A\n" + 
                "my B\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedFlip12String, actualFlip12String, "Two players flipped two valid cards");
        final String actualScoreAfterFlip1 = parseResultedString(server, score);
        final String expectedScoreAfterFlip1Option1 = "p1 0\n" + "p2 0\n";
        final String expectedScoreAfterFlip1Option2 = "p2 0\n" + "p1 0\n";
        assertTrue((actualScoreAfterFlip1.equals(expectedScoreAfterFlip1Option2) ||
                actualScoreAfterFlip1.equals(expectedScoreAfterFlip1Option1)), 
                "Score should be 0");
        // p1 flips second card A
        final URL p1flip13A = new URL("http://localhost:" + server.port() + "/flip/p1/1,3");
        final String actualFlip13String = parseResultedString(server, p1flip13A);
        final String expectedFlip13String = "3x3\n" + 
                "my A\n" + 
                "up B\n" + 
                "my A\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedFlip13String, actualFlip13String, 
                "Should have two cards I own");
        final String actualScoreAfterFlip2 = parseResultedString(server, score);
        final String expectedMatchScoreAfterFlip2Option1 = "p1 1\n" + "p2 0\n";
        final String expectedMatchScoreAfterFlip2Option2 = "p2 0\n" + "p1 1\n";
        // p1 flipped two matching cards should have 1 points and still locked
        assertTrue((actualScoreAfterFlip2.equals(expectedMatchScoreAfterFlip2Option2) ||
                actualScoreAfterFlip2.equals(expectedMatchScoreAfterFlip2Option1)),
                "p1 should have 1 points");
        // p2 flips second card A (1,3) which is still locked by player 1
        final URL p2flip13A = new URL("http://localhost:" + server.port() + "/flip/p2/1,3");
        final String actualP2Flip13String = parseResultedString(server, p2flip13A);
        final String expectedP2Flips12String = "3x3\n" + 
                "up A\n" + 
                "up B\n" + 
                "up A\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedP2Flips12String, actualP2Flip13String, 
                "should have three cards up");
        final URL p1flip13AAgain = new URL("http://localhost:" + server.port() + "/flip/p1/1,3");
        final URL p2flip21B = new URL("http://localhost:" + server.port() + "/flip/p2/2,1");
        parseResultedString(server, p1flip13AAgain);
        final String finalBoardStat = parseResultedString(server, p2flip21B);
        final String expectedFinalString = "3x3\n" + 
                "none\n" + 
                "down\n" + 
                "none\n" + 
                "my B\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedFinalString,finalBoardStat, "cards should have been removed");
        server.stop();
        
    }
    
    /*
     * 
     * 
     *      Testing partitions for flip:
     *          Input:
     *              Number of players: =1,
     *              Player.state = Fresh, One_Card_Valid
     *                             Two_cards_match
     *              Number of cards owned by other players: =0
     *              Number of removed cards: =0, >1
     *              Card to flip status: face down, removed
     *         Output:
     *              Player takes card
     *      
     * 
     *      Testing Partitions for look:
     *          Input:
     *              Number of players: =1
     *              Player.state = Fresh, One_Card_Valid,
     *                             Two_cards_match
     *              Number of cards owned by other players: =0
     *              Number of removed cards: =0, >1
     *         Output:
     *              Valid string based on the grammar of the web server
     *              
     *              
     *      
     *      Testing Partitions for Score:
     *          Input:
     *              Number of players: 0, 1
     *              Player attempted: true, false
     *              ** Mix between player number of player and if each attempted **
     *              Score value: =0, >0
     *          Output:
     *              Empty - no player attempted or 0 players
     *              Not empty - corresponds to web grammar
     */
    
    @Test
    public void testOnePlayerTwoCardsMatch() throws IOException{
        final Board board = makeBoard();
        final WebServer server = new WebServer(board, 0);
        server.start();
        final URL look = new URL("http://localhost:" + server.port() + "/look/p1");
        String actualLookInitialString = parseResultedString(server, look);
        final String expectedInitialLook = "3x3\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedInitialLook, actualLookInitialString, "All cards should be down");
        final URL score = new URL("http://localhost:" + server.port() + "/scores"); 
        String actualInitialScores = parseResultedString(server, score);
        assertEquals("", actualInitialScores, "No attemp should be empty");
        final URL flip11A = new URL("http://localhost:" + server.port() + "/flip/p1/1,1");
        final String actualFlip11String = parseResultedString(server, flip11A);
        final String expectedFlip11String = "3x3\n" + 
                "my A\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedFlip11String, actualFlip11String, "Should only have once card flipped");
        final String actualScoreAfterFlip = parseResultedString(server, score);
        final String expectedScoreAfterFlip = "p1 0\n";
        assertEquals(actualScoreAfterFlip, expectedScoreAfterFlip, "score should be 0");
        final URL flip13A = new URL("http://localhost:" + server.port() + "/flip/p1/1,3");
        final String actualFlip13String = parseResultedString(server, flip13A);
        final String expectedFlip13String = "3x3\n" + 
                "my A\n" + 
                "down\n" + 
                "my A\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedFlip13String, actualFlip13String, "Should have two cards I own");
        final String actualMatchScore = parseResultedString(server, score);
        final String expectedMatchScore = "p1 1\n";
        assertEquals(actualMatchScore, expectedMatchScore, "should have one point");
        final URL flip12B = new URL("http://localhost:" + server.port() + "/flip/p1/1,2");
        final String actualFlip12String = parseResultedString(server, flip12B);
        final String expectedFlip12String = "3x3\n" + 
                "none\n" + 
                "my B\n" + 
                "none\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n" + 
                "down\n";
        assertEquals(expectedFlip12String, actualFlip12String, "Should have two cards I own");
        final String actualflip12Score = parseResultedString(server, score);
        final String expectedflip12Score = "p1 1\n";
        assertEquals(expectedflip12Score, actualflip12Score, "should have one point");
        server.stop();
    }
    
    private static Board makeBoard() throws IOException{
        return Board.parseFromFile("boards/squareAB.txt");
    }
    private static String parseResultedString(WebServer server, URL url) throws IOException {
        final InputStream inputStream = url.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
        String parsed = "";
        String input = reader.readLine();
        while (input != null) {
            parsed += input;
            if (!input.isEmpty()) {
                parsed += "\n";
            }
            input = reader.readLine();
        }
        return parsed;
                
    }
    
    
    
}

    
    
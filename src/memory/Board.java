/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Board is a Mutable and threadsafe ADT which represents a board in a memory game
 * 
 * 
 * <p>PS4 instructions: the specifications of static methods
 * {@link #parseFromFile(String)} and {@link #generateRandom(int, int, Set)} are
 * required.
 */
public class Board {
    
    /**
     * Make a new board by parsing a file.
     * 
     * @param filename path to a game board file
     * @return a new board with the size and cards from the given file
     * @throws IOException if an error occurs reading or parsing the file
     */
    public static Board parseFromFile(String filename) throws IOException {
        BufferedReader boardReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename)), "UTF-8"));
        String boardSize = boardReader.readLine();
        String[] sizes = boardSize.split("x");
        final int rows = Integer.parseInt(sizes[0]);
        final int cols = Integer.parseInt(sizes[1]);
        Card[][] board = new Card[rows][cols];
        // create card grid
        int cardNum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String cardString = boardReader.readLine();
                board[i][j] = new Card(cardString, cardNum);
                cardNum++;
            }
        }
        // close reader
        boardReader.close();

        return new Board(board, rows, cols);

    }
    
    /**
     * Make a new random board.
     * 
     * @param rows board height
     * @param columns board width
     * @param cards cards that appear on the board
     * @return a new rows-by-columns-size board filled with a random permutation
     *         of the given cards repeated in as equal numbers as possible
     */
    public static Board generateRandom(int rows, int columns, Set<String> cards) {
        final int possibleCards = cards.size();
        final int totalSpots = rows*columns;
        final List<String> cardsList =  new ArrayList<String>();
        
        final int cardRepetition = totalSpots / possibleCards;
        
        // Adding all cards with rep
        for (String card: cards) {
            for (int rep = 0; rep < cardRepetition; rep++) {
                cardsList.add(card);
            }
        }
        
        // Handling left overs
        final int leftOver = totalSpots - cardsList.size();
        List<String> allCards = new ArrayList<String>(cards);
        for (int i = 0; i < leftOver; i++) {
            Collections.shuffle(allCards);
            cardsList.add(allCards.get(0));
        }
        
        Collections.shuffle(cardsList);
        Card[][] board = new Card[rows][columns];
        int cardIndex = 0;
        for (int i=0; i<rows; i++) {
            for (int j=0; j<columns; j++) {
                board[i][j] = new Card(cardsList.get(cardIndex), cardIndex);
                cardIndex++;       
            }
        }
        return new Board(board, rows, columns);
        
        
        
    }
    
    private final Card[][] board;
    private final int rows;
    private final int cols;
    private final Map<Player, Listener> listeners = Collections.synchronizedMap(new HashMap<Player, Listener>()); 
    
    // Abstraction function:
    //      AF(cards, rows, cols, listeners) = the board represented by the Card array
    //                              where each card is placed in cards[i][j]
    //                              0 <= i < rows
    //                              0 <= j < cols
    //                              and listeners are the observers of the game waiting 
    //                              for a change to happen
    // Representation invariant:
    //          rows > 0
    //          cols > 0
    //          All cards in cards are not null
    // 
    // Safety from Rep Exposure:
    //          All fields are final
    //          The board-card array contains immutable ADT (card) which is handled
    //          in a threadsafe synchronized manner
    //          The board is being defensively copied in construction
    //  
    // Thread safety argument:
    //
    //      Board uses the monitor pattern where every access to one of board's
    //      Cards is done in a synchronized matter.
    //      All public methods that involve mutation are synchronized, 
    //      guarded by the lock on the Card object, or by a synchronized type from
    //      collections such as the synchronized map.
    //      - Avoiding deadlock: each card has a unique card number which is picked
    //                           picked by the card order on the board. The serialized order
    //                           is done by given lower card numbers precedence.
    //      Operations that do no involve mutation 
    //      
    //   
    
    public Board(Card[][] cards, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        Card[][] newBoard = new Card[rows][cols];
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                newBoard[i][j] = cards[i][j].duplicate();
            }
        }
        this.board = newBoard;
        checkRep();
    }
    
    public void checkRep() {
        assert rows > 0;
        assert cols > 0;
        assert board.length ==  rows;
        assert board[0].length == cols;
        for (int i=0; i < rows; i++ ) {
            for (int j=0; j < cols; j++) {
                assert board[i][j] != null;
            }
        }
    }
    
    public String webString(Player p) {
        final String none = "none";
        final String down = "down";
        final String up = "up ";
        final String my = "my ";
        final String nl = "\n";
        StringBuilder boardString = new StringBuilder();
        final String boardSize = rows + "x" + cols + nl;
        boardString.append(boardSize);
        for (int row=0; row < rows; row++) {
            for (int col=0; col< cols; col++) {
                Card currentCard = board[row][col];
                if (currentCard.isRemoved()) {
                    boardString.append(none + nl);
                }
                else if (currentCard.isFacingUp()) {
                    if (currentCard.isOwned()) {
                        if (currentCard.getCardOwner().equals(p)) {
                            boardString.append(my + currentCard.toString() + nl);
                        }
                        else {
                            boardString.append(up + currentCard.toString() + nl);
                        }
                    } else {
                        boardString.append(up + currentCard.toString() + nl);
                    }
                }
                
                
                else {
                    boardString.append(down + nl);
                }
            }
        }
        checkRep();
        return boardString.deleteCharAt(boardString.length()-1).toString();
        
    }
    
    private Card getCard(int row, int col) {
        assert row >= 0 && col >= 0;
        assert row < rows && col < cols;
        return board[row][col];
    }
    
    
    public void addListener(Player player, Listener listener) {
        listeners.put(player, listener);
        checkRep();
    }
    
    private void callListeners() {
        for (Player player:  listeners.keySet()) {
            final String webBoardString = webString(player);
            Listener listener = listeners.get(player);
            listener.boardChanged(webBoardString);
        }
        checkRep();
    }
    
    public void flipCard(Player player, int row, int col) throws InterruptedException{
        assert row >= 0 && col >= 0 && row < rows && col < cols;
        Card cardToFlip = getCard(row, col);
        player.markAttempted();
        if (player.getState().equals(PlayerState.TWO_CARDS_MATCH)) {
            // 3A: If they had turned over a matching pair, they control both cards. 
            // Now, those cards are removed from the board, and they relinquish control of them.
            List<Card> orderedCards = player.getCards();
            Card firstCard = orderedCards.get(0);
            Card secondCard = orderedCards.get(1);
            synchronized(firstCard) {
                synchronized(secondCard) {
                    player.releaseCards();
                    player.resetCardList();
                    secondCard.removeCard();
                    firstCard.removeCard();
                    callListeners();
                    secondCard.notifyAll();
                    firstCard.notifyAll();
                    player.setState(PlayerState.FRESH);
                }}}
        if (player.getState().equals(PlayerState.TWO_CARDS_NO_MATCH)) {
            // 3B:  they had turned over two non-matching cards, 
            // and relinquished control but left them face up on the board. 
            // Now, for each of those card(s), if the card is still on the board, 
            // currently face up, and currently not controlled by another player, 
            // the card is turned face down.
            List<Card> orderedCards = player.getCards();
            Card firstCard = orderedCards.get(0);
            Card secondCard = orderedCards.get(1);
            synchronized(firstCard) {
                synchronized(secondCard) {
                    for (Card card : orderedCards) {
                        if (!card.isRemoved() && card.isFacingUp()) {
                            if (!card.isOwned()) {
                                card.flipCardDown();
                                callListeners();
                            }}}
                    player.resetCardList(); 
                    player.setState(PlayerState.FRESH);
                }}}
        if (player.getState().equals(PlayerState.ONE_CARD_INVALID)) {
            // 3B:  they had turned over one card, 
            // and relinquished control but left them face up on the board. 
            // Now, for each card, if the card is still on the board, 
            // currently face up, and currently not controlled by another player, 
            // the card is turned face down.
            Card playerFirstcard = player.getFirstCard();
            synchronized(playerFirstcard) {
                if (!playerFirstcard.isRemoved() && playerFirstcard.isFacingUp()) {
                    if (!playerFirstcard.isOwned()) {
                        playerFirstcard.flipCardDown();
                        callListeners();
                    }}
                player.resetCardList();
                player.setState(PlayerState.FRESH);}}
        if (player.getState().equals(PlayerState.ONE_CARD_VALID)) {
            // Turn over second card
            Card playerFirstcard = player.getFirstCard();
            Card syncFirst;
            Card syncSecond;
            if (playerFirstcard.getCardNumber() < cardToFlip.getCardNumber()) {
                syncFirst = playerFirstcard;
                syncSecond = cardToFlip;
            } else {
                syncFirst = cardToFlip;
                syncSecond = playerFirstcard;
            }
            synchronized(syncFirst) {
                synchronized(syncSecond) {
                // 2A: If there is no card there, the operation fails. 
                // The player also relinquishes control of their 
                // first card (but it remains face up for now).
                if (cardToFlip.isRemoved()) {
                    player.setState(PlayerState.ONE_CARD_INVALID);
                    playerFirstcard.releaseCard();
                    playerFirstcard.notifyAll();
                    return;
                }
                // 2B: if the card is face up and controlled by a 
                // player (another player or themselves), the operation fails. 
                // To avoid deadlocks, the operation does not block. 
                // The player also relinquishes control of their first card 
                // (but it remains face up for now).
                else if (cardToFlip.isFacingUp() && cardToFlip.isOwned()) {
                    playerFirstcard.releaseCard();
                    playerFirstcard.notifyAll();
                    player.setState(PlayerState.ONE_CARD_INVALID);
                }
                // If the card is face down, or if the card 
                // is face up but not controlled by a player
                else if (!cardToFlip.isFacingUp() ||
                        (cardToFlip.isFacingUp() && !cardToFlip.isOwned())) {
                    // 2C: If it is face down, it turns face up
                    if (!cardToFlip.isFacingUp()) {
                        cardToFlip.flipCardUp();
                        callListeners();
                    }
                    player.takeCard(cardToFlip);
                    // 2D: If the two cards are the same, that’s a successful match! 
                    // The player keeps control of both cards 
                    // (and they remain face up on the board for now). 
                    // The player’s score increases by one point.
                    if (playerFirstcard.getCardValue().equals(cardToFlip.getCardValue())) {
                        player.setState(PlayerState.TWO_CARDS_MATCH);
                        player.incrementScore();
                    }
                    // 2E: If they are not the same, 
                    // the player relinquishes control of both cards 
                    // (again, they remain face up for now)
                    else {
                        player.releaseCards();
                        player.setState(PlayerState.TWO_CARDS_NO_MATCH);
                        playerFirstcard.notifyAll();
                        cardToFlip.notifyAll();
                    }}}}}
        // Fresh turn, player does not own any cards
        if (player.getState().equals(PlayerState.FRESH)) {
            assert player.numberOwnedCards() == 0;
            synchronized(cardToFlip) {
                // 1A: If there is no card there (the player identified an empty space, 
                // perhaps because the card was just removed by another player), 
                // the operation fails.
                if (cardToFlip.isRemoved()) {
                    return;
                }
                // 1B: If the card is face down, 
                // it turns face up (all players can now see it) 
                // and the player controls that card
                else if (!cardToFlip.isFacingUp()) {
                    cardToFlip.flipCardUp();
                    player.takeCard(cardToFlip);
                    player.setState(PlayerState.ONE_CARD_VALID);
                    callListeners();
                }
                // 1C: If the card is already face up, but not controlled by another player, 
                // then it remains face up, and the player controls the card.
                else if (cardToFlip.isFacingUp() && !cardToFlip.isOwned()) {
                    player.takeCard(cardToFlip);
                    player.setState(PlayerState.ONE_CARD_VALID);
                }
                // 1D: if the card is face up and controlled by another player, 
                // the operation blocks. The player will contend with other players 
                // to take control of the card at the next opportunity.
                else if (cardToFlip.isFacingUp() && cardToFlip.isOwned()) {
                    cardToFlip.wait();
                    flipCard(player, row, col);
                }}}
        checkRep();
        }
    
    /**
     * Returns the string representation of board
     * The first line will include the board size:
     * "Board size: ixj", then in a new block:
     * cards between each col are seperated by space, 
     * each row is in a new line
     */
    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        final String boardSize = "Board Size: " + rows + "x" + cols + "\n";
        boardString.append(boardSize);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols-1; j++) {
                boardString.append(board[i][j].toString());
                boardString.append(" ");
            }
            boardString.append(board[i][cols-1].toString());
            boardString.append("\n");
        }
        checkRep();
        return boardString.toString();
    }
    
    /**
     * 
     * @return the rows size of the board
     */
    public int getRows() {
        return rows;
    }
    
    /**
     * 
     * @return the cols size of the board
     */
    public int getCols() {
        return cols;
    }
    
    
}

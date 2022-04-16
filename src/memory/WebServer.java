/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import memory.web.ExceptionsFilter;
import memory.web.HeadersFilter;
import memory.web.LogFilter;

/**
 * HTTP web game server.
 * 
 * <p>PS4 instructions: the specifications of {@link #WebServer(Board, int)},
 * {@link #port()}, {@link #start()}, and {@link #stop()} are required.
 */
public class WebServer {
    
    private final HttpServer server;
    private final Map<String, Player> players = 
            Collections.synchronizedMap(new HashMap<String, Player>());
    private final Board board;
    
    
    // Abstraction function:
    /*
     * AF(server, board, players) = The server of which the board game we are playing is
     *                             listening to and which is represented by board that has
     *                             players which are players.value();
     */
    // Representation invariant:
    /*
     *  values of players is not null
     *  The board is valid according to its rep invariant
     */
    // Safety from rep exposure:
    /*
     * The client will not be able to reach our variables as they are all
     * private and final
     */
    // Thread safety argument:
    /*
     * Are only mutable type is the players which is managed in a synchronized manner
     * by using synchronized map.
     * Our other datatypes; Player and Board are both threadsafe.
     */
    
    private static final  int SUCCESS = 200;
    private static final  int ERROR = 404;
    
    /**
     * Make a new web game server using board that listens for connections on port.
     * 
     * @param board shared game board
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    public WebServer(Board board, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.board = board;
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());
        
        HeadersFilter headers = new HeadersFilter(Map.of(
                // allow requests from web pages hosted anywhere
                "Access-Control-Allow-Origin", "*",
                // all responses will be plain-text UTF-8
                "Content-Type", "text/plain; charset=utf-8"
                ));
        List<Filter> filters = List.of(new ExceptionsFilter(), new LogFilter(), headers);
        
        // handle requests for paths that start with /look/, e.g. /look/player
        HttpContext look = server.createContext("/look/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleLook(exchange);
            }
        });
        look.getFilters().addAll(filters);
        
        // handle requests for paths that start with /flip/, e.g. /flip/player/row,column
        HttpContext flip = server.createContext("/flip/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                try {
                handleFlip(exchange);
                } catch (InterruptedException interrupt) {
                    interrupt.printStackTrace();
                }
            }
        });
        flip.getFilters().addAll(filters);
        // handle requests for paths that start with /scores, e.g. /scores
        HttpContext scores = server.createContext("/scores", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleScore(exchange);
            }
        });
        scores.getFilters().addAll(filters);
        // handle requests for paths that start with /watch/, e.g. /watch/player
        HttpContext watch = server.createContext("/watch/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                handleWatch(exchange);
            }
        });
        watch.getFilters().addAll(filters);
        checkRep();
    }
    
    /*
     * Will check that our rep is conserved
     */
    private void checkRep() {
        for (String id: players.keySet()) {
            assert players.get(id) != null;
        }
        assert server != null;
        assert board != null;
    }
    
    private Player getPlayer(String playerId) {
        Player player;
        if (!players.containsKey(playerId)) {
            player = new Player(playerId);
            players.put(playerId, player);
        }
        return players.get(playerId);
    }
    
    /*
     * Handle a request for /score by responding with the current score
     *  for all players that attempted to flip a card
     * 
     * @param exchange HTTP request/response, modified by this method to send a
     *                 response to the client and close the exchange
     */
    private void handleScore(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String response;
        String boardString = "";
        exchange.sendResponseHeaders(SUCCESS, 0);
        int numLines = 0;
        for (Player player: players.values()) {
            if (player.getAttempted()) {
                boardString += player.webScoreString() + "\n";
                numLines++;
            }
        }
        if (boardString.length() > 0) {
            boardString = boardString.substring(0, boardString.length()-1);
        }
        response = boardString;
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.println(response);
        exchange.close();
    } 
    
    /*
     * Handle a request for /watch/<playerId> by responding with the current state
     *   of the board in the perspective of the playerId
     *   <playerId> is any number of  word characters:
     *   A word character is a character from a-z, A-Z, 0-9, including the _ (underscore) character.
     *   will results error 404 otherwise. A new state of the game will be sent in the following
     *   Scenarios:
     *   - Player holds 0 cards, flips a card that was face down
     *   - Player holds 1 card, flips a card that was face down
     *   - Player holds 2 cards and they match, both removed cards will be sent at the same time
     *   - Player holds 2 cards which don't match, if both cards
     *   are not locked by other player, will send update for both together, Otherwise 
     *   just for one. 
     * 
     * @param exchange HTTP request/response, modified by this method to send a
     *                 response to the client and close the exchange
     */
    private void handleWatch(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String playerId = path.substring(base.length());
        
        final String response;
        if (playerId.matches("\\w+")) {
            Player player = getPlayer(playerId);
            exchange.sendResponseHeaders(SUCCESS, 0);
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            board.addListener(player, new Listener() {
                public void boardChanged(final String boardString) {
                    out.println(boardString);
                    exchange.close();
                }
            });
            
        } else {
            exchange.sendResponseHeaders(ERROR, 0);
            response = "Player ID does not follow the requirements";
            OutputStream body = exchange.getResponseBody();
            PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
            out.println(response);
            exchange.close();
            
        }
        
    }
    
    /*
     * Handle a request for /look/<playerId> by responding with the current state
     *   of the board in the perspective of the playerId
     *   <playerId> is any number of  word characters:
     *   A word character is a character from a-z, A-Z, 0-9, including the _ (underscore) character.
     *   will results error 404 otherwise.
     * 
     * @param exchange HTTP request/response, modified by this method to send a
     *                 response to the client and close the exchange
     */
    private void handleLook(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String playerId = path.substring(base.length());
        
        final String response;
        if (playerId.matches("\\w+")) {
            Player player = getPlayer(playerId);
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(SUCCESS, 0);
            response = board.webString(player);
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR, 0);
            response = "Player ID does not follow the requirements";
        }
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.println(response);
        exchange.close();
    } 
    
    /*
     * Handle a request for /look/<playerId>/row,col by responding with the current state
     *   of the board in the perspective of the playerId
     *   <playerId> is any number of  word characters:
     *   A word character is a character from a-z, A-Z, 0-9, including the _ (underscore) character.
     *   row,col must be 0 < row <= board.row, 0 < col <= board.col
     *   will results error 404 otherwise.
     * 
     * @param exchange HTTP request/response, modified by this method to send a
     *                 response to the client and close the exchange
     */
    private void handleFlip(HttpExchange exchange) throws IOException, InterruptedException {
        final String path = exchange.getRequestURI().getPath();
        
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String playerIdandCardCooridinate = path.substring(base.length());
        final String[] breakString = playerIdandCardCooridinate.split("/");
        assert breakString.length >= 2;
        
        final String playerId = breakString[0];
        final String[] rowCol = breakString[1].split(",");
        
        assert rowCol.length == 2;
        final int row = Integer.parseInt(rowCol[0]) - 1;
        final int col = Integer.parseInt(rowCol[1]) - 1;
        
        final String response;
        
        boolean matchBoardSize = true;
        
        if (row < 0 || col < 0 || row >= board.getRows() || col >= board.getCols()) {
            matchBoardSize = false;
        }
        
        if (playerId.matches("\\w+") && matchBoardSize) {
            Player player = getPlayer(playerId);
            exchange.sendResponseHeaders(SUCCESS, 0);
            board.flipCard(player, row, col);
            response = board.webString(player);
        } else {
            exchange.sendResponseHeaders(ERROR, 0);
            response = "Player ID does not follow the requirements";
        }
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        out.println(response);
        exchange.close();
    } 
    
    
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return server.getAddress().getPort();
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        System.err.println("Server will stop");
        server.stop(0);
    }

}

package org.acquire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameAdministratorImpl;

import java.util.*;

public class State {


    // think about why singleton class makes sense or not.
    private Board board;

    private Deque<Player> players;

    private static State gameState = null;

    private State(Board board, Deque<Player> players) {
        gameState = getInstance(board, players);
    }

    private State() {
        board = new Board();
        players = new LinkedList<>();
    }

    public static State copyState(State original) {
        if (original == null)
            throw new RuntimeException("Original state is null, can't copy null");

        State copiedState = new State();

        copiedState.board = new Board(original.getBoard()); // Assuming Board has a copy constructor
        copiedState.players = new LinkedList<>(original.players); // Assuming Player class is immutable or has a copy constructor

//         Copy Players.
//         uncomment this and comment above line if Player is not immutable or does not have copy constructor
        copiedState.players = new LinkedList<>();
        for(Player player: original.players) {
            copiedState.players.add(new Player(player)); // assuming Player has copy constructor
        }

        return copiedState;
    }


    @JsonIgnore
    public static State getInstance(){
        if(!isInitialized()){
            gameState = new State();
        }
        return gameState;
    }

    @JsonIgnore
    public static boolean isInitialized(){
        return gameState!=null;
    }

    @JsonIgnore
    public static State getInstance(Board board,Deque<Player> players){
        if(players.size()<=0 || players.size()>6){
            throw new RuntimeException("Provided number of players out of bound [1,6]");
        }
        if (!isInitialized()){
            gameState = new State();
            gameState.setPlayers(players);
            gameState.setBoard(board);
        }
        return gameState;
    }

    public static State resetGameState(){
        if(gameState==null){
            return gameState;
        }
        gameState.setBoard(null);
        gameState.setPlayers(null);
        gameState = null;
        return gameState;
    }

    public static State resetGameState(Board board,Deque<Player> players){
        if(gameState==null){
            throw new RuntimeException("State not initialized to reset");
        }
        gameState.setPlayers(players);
        gameState.setBoard(board);
        return gameState;
    }
    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Deque<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player){
        players.addLast(player);
    }

    @JsonIgnore
    public Player getCurrentPlayer(){
        return players.peek();
    }

    public Player removeFirstPlayer(){
        return players.pop();
    }

    public Player removeLastPlayer(){
        return players.removeLast();
    }
    public boolean removePlayer(String playerName){
        return players.removeIf(player -> player.getPlayer()==playerName);
    }



    @JsonIgnore
    public void nextTurn(){
        Player curr = players.pop();
        players.addLast(curr);
    }

    public void setPlayers(Deque<Player> players) {
        this.players = players;
    }

    public static State parseState(JsonNode state){
        Board board = Board.parse(state.get("board"));
        Deque<Player> players = new LinkedList<>();
        for(JsonNode node:state.get("players")){
            players.addLast(Player.parsePlayer(node));
        }
        return getInstance(board, players);
    }
}

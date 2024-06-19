package org.acquire.models;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.HotelLabel;


import java.util.LinkedList;
import java.util.List;

public class Player {

    private String player;

    private long cash;

    private List<Share> shares;

    private List<Tile> tiles;

//    private String strategy;
    private IStrategy strategy;

    private boolean needsARepTile;



    public Player(String player, long cash, List<Share> shares, List<Tile> tiles) {
        this.player = player;
        this.cash = cash;
        this.shares = shares;
        this.tiles = tiles;
    }

    public Player(){
        this.shares = new LinkedList<>();
        this.tiles = new LinkedList<>();
    }

    public Player(Player otherPlayer) {
        this.player = otherPlayer.player;
        this.cash = otherPlayer.cash;

        this.shares = new LinkedList<>();
        for (Share share : otherPlayer.shares) {
            this.shares.add(new Share(share)); // assuming Share has a copy constructor
        }

        this.tiles = new LinkedList<>();
        for (Tile tile : otherPlayer.tiles) {
            this.tiles.add(new Tile(tile));
        }
        this.strategy = otherPlayer.getStrategy();
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public IStrategy getStrategy(){ return this.strategy; }
    public void setStrategy(IStrategy strategy){ this.strategy = strategy; }

    public JsonNode placeT(State gameState) {
        return strategy.placeT(this, gameState);
    }

    public List<HotelLabel> buyS(Banker banker, State gameState) {
        return strategy.buyS(banker, gameState);
    }

    public List<Node> filteredMoves(List<Node> possibleMoves){return strategy.filterNode(possibleMoves);}

    public long getCash() {
        return cash;
    }

    public void setCash(long cash) {
        this.cash = cash;
    }

    public List<Share> getShares() {
        return shares;
    }

    public void setShares(List<Share> shares) {
        this.shares = shares;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public boolean shouldGetRepTile() {
        return this.needsARepTile;
    }
    public void setGetRepTile(boolean value) {
        this.needsARepTile = value;
    }

    public static Player parsePlayer(JsonNode node){
        String playerName = node.get("player").asText();
        long cash = node.get("cash").asLong();
        List<Share> shares = new LinkedList<>();
        for(JsonNode share:node.get("shares")){
            shares.add(new Share(HotelLabel.valueOf(share.get("share").asText()),share.get("count").asInt()));
        }
        Tile[][] tiles2D = Tile.parseTiles(node.get("tiles"));
        List<Tile> tiles = new LinkedList<>();
        for(Tile[] rowTile:tiles2D){
            for(Tile tile:rowTile){
                if(tile!=null){
                    tiles.add(tile);
                }
            }
        }
        return new Player(playerName,cash,shares,tiles);
    }
}

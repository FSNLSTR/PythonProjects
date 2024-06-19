package org.acquire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Board {

    @JsonProperty("tiles")
    private Tile[][] boardTiles = new Tile[9][12];

    @JsonProperty("hotels")
    private List<Hotel> boardHotel;

    public Board(Tile[][] boardTiles, List<Hotel> boardHotel) {
        this.boardTiles = boardTiles;
        this.boardHotel = boardHotel;
    }

    public Board(){
        boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];
        boardHotel = new ArrayList<>();
    }

    //deep copy
    public Board(Board board) {
        // Create a new 2D array of the same dimensions
        this.boardTiles = new Tile[9][12];

        // Iterate over each tile and create a deep copy of it (assuming Tile has a copy constructor)
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 12; j++) {
                Tile l = board.getBoardTiles()[i][j];
                if (l != null){
                    this.boardTiles[i][j] = new Tile(l);
                }
            }
        }

        // Create a new list and make a deep copy of every hotel (assuming Hotel has a copy constructor)
        this.boardHotel = new ArrayList<>();
        for (Hotel hotel : board.boardHotel) {
            this.boardHotel.add(new Hotel(hotel));
        }
    }

    @JsonIgnore
    public Tile[][] getBoardTiles() {
        return boardTiles;
    }

    public List<Tile> getTiles(){
        List<Tile> res = new LinkedList<>();
        for(Tile[] row:boardTiles){
            res.addAll(Arrays.stream(row).filter(tile->tile!=null).collect(Collectors.toList()));
        }
        return res;
    }

    public void setBoardTiles(Tile[][] boardTiles) {
        this.boardTiles = boardTiles;
    }

    @JsonIgnore
    public List<Hotel> getBoardHotel() {
        return boardHotel;
    }

    public List<Hotel> getHotels(){
        return boardHotel;
    }
    public void setBoardHotel(List<Hotel> boardHotel) {
        this.boardHotel = boardHotel;
    }

    public int getHotelSize(HotelLabel label) {
        List<Hotel> hotels = this.getHotels(); // Assuming board holds all the Hotel objects

        for (Hotel hotel : hotels) {
            if (hotel.getLabel().equals(label)) { // Assuming Hotel has a getLabel() method to get its label
                return hotel.getTiles().size(); // Assuming Hotel has a getTiles() method to get its tiles
            }
        }

        return 0; // Return 0 if the hotel wasn't found
    }


    public static Board parse(JsonNode boardNode) {
        Tile[][] boardTiles = Tile.parseTiles(boardNode.get("tiles"));
        List<Hotel> boardHotels = Hotel.parseHotels(boardNode.get("hotels"),boardTiles);
        return new Board(boardTiles, boardHotels);
    }

    public static Board parse(JsonNode boardNode, JsonNode Label) {
        Tile[][] boardTiles = Tile.parseTiles(boardNode.get("tiles"));
        List<Hotel> boardHotels = Hotel.parseHotels(boardNode.get("hotels"), boardTiles);
        return new Board(boardTiles, boardHotels);
    }

}

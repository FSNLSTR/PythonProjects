package org.acquire.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Hotel {

    @JsonProperty("hotel")
    private HotelLabel label;

    @JsonProperty("tiles")
    private List<Tile> tiles = new ArrayList<>();

    public Hotel(HotelLabel label, List<Tile> tiles) {
        this.label = label;
        this.tiles = tiles;
    }

    public Hotel() {
        // no-arg constructor
    }

    public Hotel(Hotel otherHotel) {
        this.label = otherHotel.label;
        this.tiles = new ArrayList<>();
        for (Tile tile : otherHotel.tiles) {
            this.tiles.add(new Tile(tile));
        }
    }

    public HotelLabel getLabel() {
        return label;
    }

    public void setLabel(HotelLabel label) {
        this.label = label;
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public static List<Hotel> parseHotels(JsonNode hotelsNode, Tile[][] boardTiles) {
        // Error handling for tiles
        if (hotelsNode.size() < 0 || hotelsNode.size() > 9*12 || !hotelsNode.isArray()){
            throw new RuntimeException("Provided Hotel Tiles are Incorrect");
        }
        List<Hotel> hotelList = new ArrayList<>();
        HashSet<HotelLabel> hotelSet = new HashSet<>();
        HashSet<Tile> tileSet = new HashSet<>();
        for(JsonNode hotelNode:hotelsNode){
            String hotelLabel = AcquireGameParser.validateJsonNodeValue(hotelNode,"hotel").asText();
            AcquireGameParser.validateLabel(hotelLabel);
            List<Tile> resultHotelsTiles = new ArrayList<Tile>();
            JsonNode hotelTiles = AcquireGameParser.validateJsonNodeValue(hotelNode,"tiles");
            if(hotelTiles.size()==0 || hotelTiles.size()==1){
                throw new RuntimeException("Hotel tiles cannot be empty or Hotel cannot have just 1 tile.");
            }
            if(hotelSet.contains(HotelLabel.valueOf(hotelLabel))){
                throw new RuntimeException("Duplicate Hotel name found");
            }
            hotelSet.add(HotelLabel.valueOf(hotelLabel));
            for (JsonNode node : hotelTiles){
                String row = AcquireGameParser.validateJsonNodeValue(node,"row").asText();
                String col = "_"+ AcquireGameParser.validateJsonNodeValue(node,"column").asText();
                AcquireGameParser.validateRow(row);
                AcquireGameParser.validateColumn(col);
                BoardRow boardRow = BoardRow.valueOf(row);
                BoardColumn boardColumn = BoardColumn.valueOf(col);
                if(boardTiles!=null && boardTiles[boardRow.ordinal()][boardColumn.ordinal()]==null){
                    throw new RuntimeException("Tile not present board");
                }
                boardTiles[boardRow.ordinal()][boardColumn.ordinal()].setHotelLabel(HotelLabel.valueOf(hotelLabel));

                Tile hotelTile = new Tile(boardRow,boardColumn);
                if(tileSet.contains(hotelTile)){
                    throw new RuntimeException("Tile already present in a different hotel");
                }
                if(resultHotelsTiles.contains(hotelTile)){
                    throw new RuntimeException("Tile already exists");
                }
                hotelTile.setHotelLabel(HotelLabel.valueOf(hotelLabel));
                resultHotelsTiles.add(hotelTile);
                tileSet.add(hotelTile);
            }
            Hotel hotel= new Hotel(HotelLabel.valueOf(hotelLabel),resultHotelsTiles);
            hotelList.add(hotel);
        }
        return hotelList;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "label=" + label +
                ", tiles=" + tiles +
                '}';
    }
}

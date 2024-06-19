package org.acquire.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameParser;

public class Tile {
    private BoardRow row;

    @JsonProperty("column")

    private BoardColumn column;

    @JsonIgnore
    private HotelLabel hotelLabel;

    public Tile(BoardRow row, BoardColumn column) {
        this.row = row;
        this.column = column;
        hotelLabel = null;
    }

    public Tile(Tile otherTile) {
        this.row = otherTile.getRow();
        this.column = otherTile.getBoardColumn();
        hotelLabel = otherTile.getHotelLabel(); // Ensure hotelLabel is either null or immutable.
    }


    public HotelLabel getHotelLabel() {
        return hotelLabel;
    }

    public void setHotelLabel(HotelLabel hotelLabel) {
        this.hotelLabel = hotelLabel;
    }

    public BoardRow getRow() {
        return row;
    }

    public void setRow(BoardRow row) {
        this.row = row;
    }

    public String getColumn() {
        return column.toString().replace("_", "");
    }

    @JsonIgnore
    public BoardColumn getBoardColumn(){
        return column;
    }

    public void setColumn(BoardColumn column) {
        this.column = column;
    }

    public static Tile[][] parseTiles(JsonNode tilesNode) {

        // Error handling for tiles
        if (tilesNode.size() > 9*12 || !tilesNode.isArray()){
            throw new RuntimeException("Provided Tiles are Incorrect");
        }
        Tile[][] resultTiles = new Tile[9][12];
        for (JsonNode node : tilesNode){
            String row = AcquireGameParser.validateJsonNodeValue(node,"row").asText();
            String col = "_"+ AcquireGameParser.validateJsonNodeValue(node,"column").asText();
            AcquireGameParser.validateRow(row);
            AcquireGameParser.validateColumn(col);
            BoardRow boardRow = BoardRow.valueOf(row);
            BoardColumn boardColumn = BoardColumn.valueOf(col);
            if(resultTiles[boardRow.ordinal()][boardColumn.ordinal()]!=null){
                throw new RuntimeException("Tile already exists");
            }
            resultTiles[boardRow.ordinal()][boardColumn.ordinal()] = new Tile(boardRow,boardColumn);
        }

        return resultTiles;
    }

    @Override
    public int hashCode() {
        return row.hashCode()*column.hashCode()+1;

    }

    @Override
    public boolean equals(Object obj1) {
        if (this == obj1) {
            return true;
        }
        if (obj1 == null || getClass() != obj1.getClass()) {
            return false;
        }
        Tile obj = (Tile) obj1;
        return obj.getRow().toString().equals(this.row.toString()) && obj.getBoardColumn().toString().equals(this.column.toString());
    }


    @Override
    public String toString() {
        return "Tile{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}

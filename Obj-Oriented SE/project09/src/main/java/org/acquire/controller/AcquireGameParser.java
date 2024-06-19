package org.acquire.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;

import java.util.Optional;


public class AcquireGameParser {
    public static void validateRequest(JsonNode jsonNode, String name) {
        if(name.equals("setup") || name.equals("start")){
            validateJsonArraySize(jsonNode,"players",1,6);
            return;
        }
        if(name.equals("turn")){
            //Validate turnPlayer
            JsonNode turnPlayer = jsonNode.get("player");
            if(!turnPlayer.has("shares")){
                throw new RuntimeException("Provided JSON Object does not have 'share' field in the state: "+turnPlayer.asText());
            }
            for(JsonNode node:turnPlayer.get("shares")){
                validateShareJson(node);
            }

            if(!turnPlayer.has("cash")){
                throw new RuntimeException("Provided JSON Object does not have 'Cash' field in the state: "+jsonNode.asText());
            }
            if (turnPlayer.get("cash").asInt() < 0){
                throw new RuntimeException("Cash for player is is less than 0");
            }


            //Validate given Board
            validateBoardJson(jsonNode.get("board"));

            //Validate the tiles under player's ownership
            if(!jsonNode.get("tile").isEmpty()){
                for(JsonNode node:jsonNode.get("tile")){
                    validateTileJson(node);
                }
            }

            //Validate shares
            if(!jsonNode.get("share").isEmpty()){
                for(JsonNode node:jsonNode.get("share")){
                    validateShareJson(node);
                }
            }

//            //Validate xHotel
//            if(!jsonNode.get("xhotel").isEmpty()){
//                for(JsonNode node:jsonNode.get("xhotel")){
//                    validateHotelJson(node);
//                }
//            }
            return;

        }
        if(!jsonNode.has("state")){
            throw new RuntimeException("Provided JSON Object does not have 'state' field: "+jsonNode.asText());
        }
        if(name.equals("buy")){
            validateJsonArraySize(jsonNode,"shares",1,3);

            return;
        }
        String row = validateJsonNodeValue(jsonNode,"row").asText();
        String col = "_"+validateJsonNodeValue(jsonNode,"column").asText();
        validateRow(row);
        validateColumn(col);
        if (name.equals("place") && jsonNode.has("label")) {
            String label = validateJsonNodeValue(jsonNode,"label").asText();
            validateLabel(label);
        }
        JsonNode state = jsonNode.get("state");
        validateStateJson(state);
    }

    public static void validateStateJson(JsonNode jsonNode){
        if(!jsonNode.has("board")){
            throw new RuntimeException("Provided JSON Object does not have 'board' field in the state: "+jsonNode.asText());
        }
        validateJsonArraySize(jsonNode,"players",1,6);
        validateBoardJson(jsonNode.get("board"));
    }

    public static void validateBoardJson(JsonNode jsonNode){
        if(!jsonNode.has("tiles")){
            throw new RuntimeException("Provided JSON Object does not have 'tiles' field in the state: "+jsonNode.asText());
        }
        if(!jsonNode.get("tiles").isArray()){
            throw new RuntimeException("Provided JSON Object does not have a 'tiles' as a array/list in the state: "+jsonNode.asText());
        }
        if(jsonNode.get("tiles").size()>0){
            for(JsonNode node:jsonNode.get("tiles")){
                validateTileJson(node);
            }
        }

        if(!jsonNode.has("hotels")){
            throw new RuntimeException("Provided JSON Object does not have 'hotels' field in the state: "+jsonNode.asText());
        }
        if(!jsonNode.get("hotels").isArray()){
            throw new RuntimeException("Provided JSON Object does not have a 'hotels' as a array/list in the state: "+jsonNode.asText());
        }
        if(jsonNode.get("hotels").size()>0){
            for(JsonNode node:jsonNode.get("hotels")){
                validateHotelJson(node);
            }
        }
    }
    public static void validateHotelJson(JsonNode jsonNode){
        if(!jsonNode.has("hotel")){
            throw new RuntimeException("Provided JSON Object does not have 'hotel' field in the state: "+jsonNode.asText());
        }
        validateLabel(jsonNode.get("hotel").asText());
        if(!jsonNode.has("tiles")){
            throw new RuntimeException("Provided JSON Object does not have 'tiles' field in the state: "+jsonNode.asText());
        }
        if(!jsonNode.get("tiles").isArray()){
            throw new RuntimeException("Provided JSON Object does not have a 'tiles' as a array/list in the state: "+jsonNode.asText());
        }
        for(JsonNode node:jsonNode.get("tiles")){
            validateTileJson(node);
        }


    }

    public static void validateTileJson(JsonNode jsonNode){
        if(!jsonNode.has("row")){
            throw new RuntimeException("Provided JSON Object does not have 'row' field in the state: "+jsonNode.asText());
        }
        validateRow(jsonNode.get("row").asText());
        if(!jsonNode.has("column")){
            throw new RuntimeException("Provided JSON Object does not have 'column' field in the state: "+jsonNode.asText());
        }
        validateColumn("_"+jsonNode.get("column").asText());


    }

    public static void validateShareJson(JsonNode jsonNode){
        if(!jsonNode.has("share")){
            throw new RuntimeException("Provided JSON Object does not have 'share' field in the state: "+jsonNode.asText());
        }
        String label = validateJsonNodeValue(jsonNode,"share").asText();
        validateLabel(label);

        if(!jsonNode.has("count")){
            throw new RuntimeException("Provided JSON Object does not have 'count' field in the state: "+jsonNode.asText());
        }
        if (jsonNode.get("count").asInt() < 0){
            throw new RuntimeException("Share count for "+ label+ " is less than 0");
        }
    }
    public static void validateJsonArraySize(JsonNode jsonNode,String field,int lowerBound,int upperBound){
        if(!jsonNode.has(field)){
            throw new RuntimeException("Provided JSON Object does not have '"+field+"' field in the state: "+jsonNode.asText());
        }
        if(!jsonNode.get(field).isArray()){
            throw new RuntimeException("Provided JSON Object does not have a '"+field+"' as a array/list in the state: "+jsonNode.asText());
        }
        if(jsonNode.get(field).size()<lowerBound || jsonNode.get(field).size()>upperBound){
            throw new RuntimeException("Provide '"+field+"' array length"+jsonNode.get(field).size()+" is out of bounds ["+lowerBound+","+upperBound+"].");
        }
    }

    public static JsonNode validateJsonNodeValue(JsonNode node,String key){
        return Optional.ofNullable(node.get(key)).orElseThrow(()-> new RuntimeException("Invalid key:"+key));
    }

    public static void validateRow(String row) {
        try{
            BoardRow row1  = BoardRow.valueOf(row);
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Invalid row:"+row);
        }
    }

    public static void validateColumn(String col) {
        try{
            BoardColumn row1  = BoardColumn.valueOf(col);
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Invalid column:"+col);
        }
    }

    public static void validateLabel(String label) {
        try{
            HotelLabel row1  = HotelLabel.valueOf(label);
        }catch (IllegalArgumentException e){
            throw new RuntimeException("Invalid Label:"+label);
        }
    }

}

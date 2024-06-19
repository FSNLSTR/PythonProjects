package org.acquire;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.*;
import org.acquire.models.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        //String jsonData = "{\"request\": \"start\",\"players\": [{ \"player\" : \"AI_1\", \"strategy\" : \"ordered\"}, { \"player\" : \"AI_2\", \"strategy\" : \"random\"}, { \"player\" : \"AI_3\", \"strategy\" : \"largest alpha\"}, { \"player\" : \"AI_4\", \"strategy\" : \"smallest anti\"}]}";
        //String jsonData = "{\"request\": \"setup\",\"players\": [\"AI_1\",\"AI_2\"]}";
        String jsonData = "{\"request\": \"start\",\"players\": [{ \"player\" : \"AI_1\", \"strategy\" : \"smallest anti\"}, { \"player\" : \"AI_2\", \"strategy\" : \"largest alpha\"}]}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonData);

        JsonNode jsonResponse = processJson(jsonNode);
        System.out.println(jsonResponse);
    }

    public static JsonNode processJson(JsonNode jsonNode) {
        JsonNode jsonResponse = null;
        AcquireGameParser gameParser = new AcquireGameParser();
        IAcquireGameEngine gameEngine = new AcquireGameEngineImpl();
        AcquireGameAdministratorImpl admin;
        State state = null;
        Banker banker;
        try {
            String requestType = jsonNode.get("request").asText();

            switch (requestType) {
                case "setup":
                     admin = new AcquireGameAdministratorImpl();

                    gameParser.validateRequest(jsonNode, "setup");
                    JsonNode players= jsonNode.get("players");
                    List<String> playersList = new LinkedList<>();
                    for(JsonNode node:players){
                        if(node.isNull()){
                            return AcquireGameHelper.createErrorJson("A player name can not be null");
                        }
                        playersList.add(node.asText());
                    }
                    jsonResponse = admin.initiateGame(playersList);
                    break;
                case "start":
                    HashMap<String, Integer> winners= new HashMap<>();
                    gameParser.validateRequest(jsonNode, "start");
                    JsonNode auto_players = jsonNode.get("players");
                    List<String> playerNames = new ArrayList<String>();

                    for(JsonNode node:auto_players){
                        if(node.isNull()){
                            return AcquireGameHelper.createErrorJson("A player name can not be null");
                        }
                        String playerName = node.get("player").asText();
                        playerNames.add(playerName);
                    }
                    for (int i = 0; i < 1; i++) {
                        admin = new AcquireGameAdministratorImpl();
                        admin.initiateGame(playerNames);
                        jsonResponse = admin.startGame1(auto_players);
                        Player winner = admin.getWinningPlayer();

                        winners.put(winner.getStrategy().toString(), winners.getOrDefault(winner.getStrategy().toString(), 0) + 1);
                        admin.resetGame();
                        System.out.println("###################################################################");
                    }

                    // Write results to text file
                    try (PrintWriter writer = new PrintWriter("game-results.txt", "UTF-8")) {
                        winners.forEach((strategy, wins) -> writer.println(strategy + " won " + wins + " games"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //jsonResponse admin.startGame(jsonNode)
                    //json response will be output of startgame, if error then return createerrorJson
                    //startGame will call admin.initiateGame() as well as check strategy and call funcs accordingly
                    //don't need to do anything else in here
                    break;
                case "turn":
                    //Validate Turn request
                    gameParser.validateRequest(jsonNode, "turn");

                    //Save board
                    Board board = Board.parse(jsonNode.get("board"));
                    state = State.getInstance();
                    state.setBoard(board);
                    banker = Banker.getBanker();

                    //Save turnPlayer info
                    JsonNode turnPlayer = jsonNode.get("player");
                    long cash = turnPlayer.get("cash").asLong();
                    admin = new AcquireGameAdministratorImpl(state, banker);
                    ArrayList<String> turnPlayerName = new ArrayList<>();
                    turnPlayerName.add(turnPlayer.get("player").asText());


                    //Initiate Game with turnPlayerName
                    admin.initiateGame(turnPlayerName);


                    //Set cash
                    state.getCurrentPlayer().setCash(cash);

                    //Set shares
                    JsonNode shareNodes = turnPlayer.get("shares");
                    List<Share> shares = new ArrayList<Share>();
                    for (JsonNode node: shareNodes){
                        String label =  node.get("share").asText();
                        int count = node.get("count").asInt();
                        shares.add(new Share(HotelLabel.valueOf(label), count));
                    }
                    state.getCurrentPlayer().setShares(shares);

                    //Set tiles
                    JsonNode tileNodes = turnPlayer.get("tiles");
                    List<Tile> tiles = new ArrayList<Tile>();
                    for (JsonNode node: tileNodes){
                        String rowVal = node.get("row").asText();
                        String colVal = "_"+node.get("column").asText();
                        BoardRow row = BoardRow.valueOf(rowVal);
                        BoardColumn col = BoardColumn.valueOf(colVal);
                        tiles.add(new Tile(row, col));
                    }
                    state.getCurrentPlayer().setTiles(tiles);

                    //Save unallocated Tiles to Banker's tilepool
                    JsonNode unallocatedTiles = jsonNode.get("tile");
                    Set<Tile> unallocTiles = new HashSet<> ();
                    for(JsonNode tileNode : unallocatedTiles){
                        BoardRow row = BoardRow.valueOf(tileNode.get("row").asText());
                        BoardColumn column = BoardColumn.valueOf("_"+tileNode.get("column").asText());
                        Tile tile = new Tile(row, column);
                        unallocTiles.add(tile);
                    }
                    banker.setTilePool(unallocTiles);

                    //Save available shares to the banker's hotel shares dictionary
                    Map<HotelLabel,Integer> hShare = new HashMap<HotelLabel,Integer>();
                    JsonNode shareCount = jsonNode.get("share");
                    for(JsonNode node: shareCount){
                        if(node.get("share") != null && node.get("count") != null) {
                            HotelLabel label = HotelLabel.valueOf(node.get("share").asText());
                            int count = node.get("count").asInt();
                            hShare.put(label, count);
                        }
                    }
                    banker.setHotelShare(hShare);
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode playerNode = mapper.createObjectNode();

                    // Copy "player" and "strategy" fields from turnPlayer
                    playerNode.put("player", turnPlayer.get("player").asText());
                    playerNode.put("strategy", turnPlayer.get("strategy").asText());

                    // Create new array node and add playerNode to it
                    ArrayNode arrayNode = mapper.createArrayNode();
                    arrayNode.add(playerNode);

                    admin.setStrat(arrayNode);
                    jsonResponse = admin.turn();
                    admin.resetGame();
                    break;

                case "place":

                    gameParser.validateRequest(jsonNode, "place");
                    state = State.parseState(jsonNode.get("state"));
                    banker = Banker.getBanker();
                    admin = new AcquireGameAdministratorImpl(state,banker);

//                    System.out.println((new ObjectMapper()).valueToTree(singletonBoard.getBoardTiles()[2][1]));
                    BoardRow row = BoardRow.valueOf(gameParser.validateJsonNodeValue(jsonNode, "row").asText());
                    BoardColumn col = BoardColumn.valueOf("_" +gameParser.validateJsonNodeValue(jsonNode, "column").asText());
                    if(jsonNode.has("hotel")){
                        HotelLabel label = HotelLabel.valueOf(gameParser.validateJsonNodeValue(jsonNode, "hotel").asText());
                        jsonResponse = admin.placeTile(row,col,label);
                    }else{
                        jsonResponse = admin.placeTile(row,col);
                    }
                    admin.resetGame();
                    break;
                case "buy":
                    gameParser.validateRequest(jsonNode, "buy");
                    state = State.parseState(jsonNode.get("state"));
                    banker = Banker.getBanker();
                    admin = new AcquireGameAdministratorImpl(state,banker);
                    JsonNode hotelsNode = AcquireGameParser.validateJsonNodeValue(jsonNode, "shares");
                    List<HotelLabel> hotels = new LinkedList<>();
                    for(JsonNode hotel:hotelsNode){
                        AcquireGameParser.validateLabel(hotel.asText());
                        hotels.add(HotelLabel.valueOf(hotel.asText()));
                    }
                    jsonResponse = admin.buyStock(hotels);
                    admin.resetGame();
                    break;
                case "done":
                    gameParser.validateRequest(jsonNode, "done");
                    state = State.parseState(jsonNode.get("state"));
                    banker = Banker.getBanker();
                    admin = new AcquireGameAdministratorImpl(state,banker);
                    if(admin.endTurn()){
                        jsonResponse = (new ObjectMapper()).valueToTree(state);
                    }else{
                        jsonResponse = AcquireGameHelper.createImpossibleJson("No available tiles for current player, but changed the player chance");

                    }
                    admin.resetGame();
                    break;
//                case "merging":
//                    gameParser.validateRequest(jsonNode, "merging");
//                    Board mergingBoard = Board.parse(jsonNode.get("board"));
//                    HotelLabel hotelLabelMerging = HotelLabel.valueOf(AcquireGameParser.validateJsonNodeValue(jsonNode, "label").asText());
//                    jsonResponse = gameEngine.merging(mergingBoard, BoardRow.valueOf(jsonNode.get("row").asText()),BoardColumn.valueOf("_" + jsonNode.get("column").asText()),hotelLabelMerging);
//                    break;
                default:
                    jsonResponse = AcquireGameHelper.createErrorJson("Error processing the request."+jsonNode.toPrettyString());

            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse = AcquireGameHelper.createErrorJson(e.getMessage());
        }
        return jsonResponse;
    }
}


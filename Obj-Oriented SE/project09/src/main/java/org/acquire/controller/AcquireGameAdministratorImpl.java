package org.acquire.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.models.*;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Collections.copy;
import static java.util.Collections.sort;

public class AcquireGameAdministratorImpl implements IAcquireGameAdministrator{

    private static final int INITIAL_CASH = 6_000;

    private final ObjectMapper mapper = new ObjectMapper();

    private final AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

    private GameTree gameTree = null;
    private State gameState;

    private Banker banker;

    private Node currentNode;

    public AcquireGameAdministratorImpl(State gameState, Banker banker) {
        this.gameState = gameState;
        this.banker = banker;
    }

    public AcquireGameAdministratorImpl() {
        gameState = State.getInstance();
        banker = Banker.getBanker();

    }

    public void destroy(){
        State.resetGameState();
        Banker.destroyBanker();
    }
    @Override
    public JsonNode initiateGame(List<String> players) {
        if(players.size()<1 || players.size()>6){
            return AcquireGameHelper.createErrorJson("Provided Players are not in range [1,6]");
        }
        for(String playerName:players){
            if(playerName==null){
                return AcquireGameHelper.createErrorJson("A player name can not be null");
            }
            if(playerName.length()>20 || playerName.trim().length()==0){
                return AcquireGameHelper.createErrorJson("Player name can not be greater than 20 characters");
            }
            List<Tile> playerTiles = new LinkedList<>();
            for(int i=0;i<6;i++){
                playerTiles.add(banker.getRandomTile());
            }
            gameState.addPlayer(new Player(playerName.trim(),INITIAL_CASH,new LinkedList<>(),playerTiles));
        }

        return mapper.valueToTree(gameState);
    }

    @Override
    public JsonNode placeTile(BoardRow row, BoardColumn col) {
        JsonNode queryResult = gameEngine.query(gameState.getBoard(), row, col);
        if(!AcquireGameHelper.checkPlayerHasTile(gameState.getCurrentPlayer(), new Tile(row,col))){
            return AcquireGameHelper.createImpossibleJson("The current player does not have the provided tile");
        }
        if(queryResult.has("growing")){
            //System.out.println("Growing...");
            gameEngine.growing(gameState.getBoard(), row, col);
        }else if(queryResult.has("singleton")){
            //System.out.println("Placing a Singleton...");
            gameEngine.singleton(gameState.getBoard(),row,col);
        }else if(queryResult.has("impossible") || queryResult.has("error")){
            return AcquireGameHelper.createImpossibleJson("Can not grow/place the tile");
        }
        return mapper.valueToTree(gameState);
    }

    public JsonNode placeTile(BoardRow row, BoardColumn col, HotelLabel label){
        if(!AcquireGameHelper.checkPlayerHasTile(gameState.getCurrentPlayer(), new Tile(row,col))){
            return AcquireGameHelper.createImpossibleJson("The current player does not have the provided tile");
        }
        JsonNode queryResult = gameEngine.query(gameState.getBoard(), row, col,label);
        if(queryResult.has("founding")){
            //System.out.println("Founding "+label+"....");
            gameEngine.founding(gameState.getBoard(), row,col,label);
            Player currentPlayer = gameState.getCurrentPlayer();
            if(banker.getCurrentHotelShareCount(label)>0){
                updateShareOfPlayer(currentPlayer, label);
            }

        } else if (queryResult.has("acquirer")) {

            JsonNode acquired = queryResult.get("acquired");
            if (acquired.isArray()) {
                for (JsonNode element : acquired) {

                    HotelLabel hLabel = HotelLabel.valueOf(element.asText());
                    Hotel hotel = null;
                    List<Hotel> hotels = gameState.getBoard().getHotels();
                    for (Hotel h: hotels){
                        if (h.getLabel() == hLabel){
                            hotel = h;
                        }
                    }

                    // Do something with element
                    HashMap<String,List<Player>> majorityMinorityPlayers = getMajorityAndMinorityHolders(hLabel);
                    int hotelSize = hotel.getTiles().size();
                    settleAllBonuses(majorityMinorityPlayers, hLabel, hotelSize);
                    //System.out.println(queryResult.get("acquirer").asText() + " acquired "+element.asText());
                }
            }
            gameEngine.merging(gameState.getBoard(), row, col, label);

            for (Hotel hotel : gameState.getBoard().getHotels()) {
                List<Tile> checkedTiles = new ArrayList<>();
                for (Tile tile : hotel.getTiles()) {
                    if (!checkedTiles.contains(tile)) {
                        checkedTiles.add(tile);
                    }
                }
                hotel.setTiles(checkedTiles);
            }


        } else if (queryResult.has("error") || queryResult.has("impossible")) {
            return AcquireGameHelper.createImpossibleJson("Can not found or merge any hotels");
        }

        return mapper.valueToTree(gameState);
    }

    @Override
    public JsonNode buyStock(List<HotelLabel> labels) {
        if (labels.size() > 3 || labels.isEmpty()) {
            return AcquireGameHelper.createErrorJson("Provided hotels can not be empty and greater than 3!");
        }

        Player currentPlayer = gameState.getCurrentPlayer();
        List<Hotel> hotels = gameState.getBoard().getHotels();

        for(HotelLabel shareLabel: labels){
            // Check if the hotel exists on the board

            Hotel foundHotel = null;
//            Hotel foundHotel = hotels.stream().filter(hotel -> hotel.getLabel()==shareLabel).findAny().orElse(null);
            for (Hotel hotel : hotels) {
                if (hotel.getLabel() == shareLabel) {
                    foundHotel = hotel;
                    break;
                }
            }
            if (foundHotel != null) {
//                int tilesOwned = 0;
//                for (Tile tile : currentPlayer.getTiles()) {
//                    if (foundHotel.getTiles().contains(tile)) {
//                        tilesOwned++;
//                    }
//                }
                if(banker.getCurrentHotelShareCount(shareLabel)<=0){
                    return AcquireGameHelper.createImpossibleJson("No available share for "+shareLabel);
                }
                int price = banker.getCertificatePrice(shareLabel, foundHotel.getTiles().size());

                long newCash = currentPlayer.getCash() - price;
                if (newCash < 0) {
                    return AcquireGameHelper.createImpossibleJson("Current Player:"+currentPlayer.getPlayer()+" does not enough money to buy share of price:"+price+" with "+currentPlayer.getCash()+" cash.");
                }
                currentPlayer.setCash(newCash);

                updateShareOfPlayer(currentPlayer, shareLabel);
                banker.decrementHotelShareCount(shareLabel, 1);
            } else {
                return AcquireGameHelper.createErrorJson("No in-play hotel chain found with provided label");
            }
        }
        return mapper.valueToTree(gameState);
    }

    //Increments player's shares in that hotel, or just adds a share of that hotel
    private void updateShareOfPlayer(Player currentPlayer, HotelLabel shareLabel) {
        Share boughtShare = currentPlayer.getShares().stream()
                .filter(share -> share.getLabel()==shareLabel)
                .findFirst()
                .orElse(null);
        if(boughtShare==null){
            boughtShare = new Share(shareLabel,0);
            currentPlayer.getShares().add(boughtShare);
        }
        boughtShare.setCount((boughtShare.getCount()+1));
    }

    @Override
    public boolean endTurn() {
        Player currentPlayer = gameState.getCurrentPlayer();

        // Check if there are replacement tiles available
        if ((banker.getTilePool()).isEmpty()) {
            gameState.nextTurn();
            return false;
        }

        //Add a replacement for the tile that got placed
        if (currentPlayer.shouldGetRepTile()){
            Tile replacementTile = banker.getRandomTile();
            currentPlayer.getTiles().add(replacementTile);
            currentPlayer.setGetRepTile(false);
        }

        List<Tile> tiles = currentPlayer.getTiles();

        // Helper function to flush out as many permanently unplayable tiles as possible given the # of replacement tiles left
        UnaryOperator<Tile> replaceUnplayable = (tile) -> {
            if (AcquireGameHelper.isPermUnplayable(gameState.getBoard(), tile)) {
                if (banker.getTilePool().isEmpty()){
                    return tile;
                }
                return banker.getRandomTile();  // returns a new tile from the banker if the tile is unplayable
            }
            return tile;  // returns the same tile if it is playable
        };

        // Replaces all unplayable tiles with new ones
        tiles.replaceAll(replaceUnplayable);


        // Go to next player
        gameState.nextTurn();

        return true;
    }

    @Override
    public boolean isGameOver() {
        return false;
    }

    private HashMap<String,List<Player>> getMajorityAndMinorityHolders(HotelLabel label){
        HashMap<String,List<Player>> res = new HashMap<>();
        int majority = Integer.MIN_VALUE,minority = Integer.MIN_VALUE;
        for(Player player: gameState.getPlayers()){
            for(Share share:player.getShares()){
                if(share.getLabel()!=label)continue;
                int value = share.getCount();
                if (value > majority) {
                    minority = majority; // Update second max before updating max
                    majority = value;
                } else if (value > minority && value != majority) {
                    minority = value;
                }
            }
        }

        List<Player> majorityPlayers = new LinkedList<>(),
                minorityPlayers = new LinkedList<>();
        for(Player player: gameState.getPlayers()){
            for(Share share:player.getShares()){
                if(share.getLabel()==label){
                    if(share.getCount()==majority){
                        majorityPlayers.add(player);
                    }
                    if(share.getCount()==minority){
                        minorityPlayers.add(player);
                    }
                }
            }
        }
        res.put("majority", majorityPlayers);
        res.put("minority", minorityPlayers);
        return res;
    }

    private void settleAllBonuses(HashMap<String,List<Player>> majorityMinorityPlayers, HotelLabel label, int tilesSize){
        List<Player> majorityPlayers = majorityMinorityPlayers.get("majority"),
                minorityPlayers = majorityMinorityPlayers.get("minority");

        List<Player> majorityMinority = new ArrayList<>(majorityPlayers);
        majorityMinority.addAll(minorityPlayers);

        List<Player> allPlayers = gameState.getPlayers().stream().toList();
        for (Player p: allPlayers){
            if(!majorityMinority.contains(p)){
                for (Share s: p.getShares()){
                    if(s.getLabel() == label){
                        int liquidationVal = banker.getCertificatePrice(label, tilesSize) * s.getCount();
                        p.setCash(p.getCash() + liquidationVal);
                    }
                }
            }
            List<Share> shares = p.getShares();
            shares.removeIf(share -> share.getLabel() == label);
            p.setShares(shares);
        }

        int currentPrice = banker.getCertificatePrice(label, tilesSize),
                majorityBonus = currentPrice*10,
                minorityBonus = currentPrice*5;

        if(majorityPlayers.size()>1){
            int singleMajorityBonus = (int)Math.ceil((majorityBonus+minorityBonus)/majorityPlayers.size());
            for(Player player:majorityPlayers){
                player.setCash(player.getCash()+singleMajorityBonus);
            }
            return;
        }
        if(majorityPlayers.size()==1 && minorityPlayers.size()==0){
            int singleMajorityBonus = (int)Math.ceil(majorityBonus/majorityPlayers.size());
            for(Player player:majorityPlayers){
                player.setCash(player.getCash()+singleMajorityBonus);
            }
            return;
        }
        if(majorityPlayers.size()==1 && minorityPlayers.size()>=1){
            int singleMajorityBonus = (int)Math.ceil(majorityBonus/majorityPlayers.size()),
                    singleMinorityBonus = (int)Math.ceil(minorityBonus/minorityPlayers.size());
            for(Player player:majorityPlayers){
                player.setCash(player.getCash()+singleMajorityBonus);
            }
            for(Player player:minorityPlayers){
                player.setCash(player.getCash()+singleMinorityBonus);
            }
        }

    }

    public boolean haveAllPlayersRunOutOfTiles() {
        for (Player player : gameState.getPlayers()) {
            // If any player still has tiles, return false
            if (!player.getTiles().isEmpty()) {
                return false;
            }
        }
        // If we made it through the loop without returning, all players have run out of tiles
        return true;
    }

    public boolean areAllPlayersTilesUnplayable(){
        for (Player player : gameState.getPlayers()) {
            for (Tile tile : player.getTiles()) {
                if (AcquireGameHelper.canPlace(gameState.getBoard(), tile)) {
                    return false; // As soon as we find a playable tile, we can conclude not all tiles are unplayable
                }
            }
        }
        return true; // If we've gone through all tiles of all players and found no playable ones, then all tiles are unplayable
    }

    public HashMap<Player, Long> calculateTotalValuation() {
        HashMap<Player, Long> valuations = new HashMap<>();

        for (Player player : gameState.getPlayers()) {
            long playerValuation = player.getCash();

            for (Share share : player.getShares()) {
                Hotel currentHotel = gameState.getBoard().getBoardHotel().stream()
                        .filter(hotel -> hotel.getLabel().equals(share.getLabel()))
                        .findFirst()
                        .orElse(null);

                // Add the valuation of each share
                if (currentHotel != null){
                    playerValuation += banker.getCertificatePrice(share.getLabel(), currentHotel.getTiles().size());
                }
                else
                {
                    playerValuation += banker.getCertificatePrice(share.getLabel(), 0);
                }
            }

            valuations.put(player, playerValuation);
        }
        return valuations;
    }

    public void setStrat(JsonNode playersNode){
        Deque<Player> playerList = gameState.getPlayers();

        for (JsonNode playerNode : playersNode) {
            //System.out.println(playerNode);
            String playerName = playerNode.get("player").asText();
            String strategy = playerNode.get("strategy").asText();
            //System.out.println(strategy);
            Iterator<Player> iterator = playerList.iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (player.getPlayer().equals(playerName)) {
                    if (strategy.equals("ordered")){
                        player.setStrategy(new OrderedStrategy());
                    }else if (strategy.equals("random")) {
                        player.setStrategy(new RandomStrategy());
                    }else if (strategy.equals("largest alpha")) {
                        player.setStrategy(new largestAlphaStrategy());
                    }else if (strategy.equals("smallest anti")) {
                        player.setStrategy(new smallestAntiStrategy());
                    }
                    break;
                }
            }
        }
        gameState.setPlayers(playerList);
    }

//    public JsonNode startGame(JsonNode playersNode){
//        setStrat(playersNode);
//
//        while(!AcquireGameHelper.checkEndGameCondition(gameState.getBoard()) ){
//            Player currentPlayer = gameState.getCurrentPlayer();//While game is not over
//            System.out.println("Player "+currentPlayer.getPlayer()+"'s turn:");
//            System.out.println("All tiles: "+currentPlayer.getTiles());
//
//            JsonNode Action = turn();
//            System.out.println(Action);
//
//            //1. Placing a tile
//            if (Action.has("place")){
//                JsonNode Place = Action.get("place");
//                if (Place != null){
//                    BoardRow row = BoardRow.valueOf(Place.get("row").asText());
//                    BoardColumn col = BoardColumn.valueOf(Place.get("column").asText());
//                    if (Place.has("hotel")){
//                        HotelLabel label = HotelLabel.valueOf(Place.get("hotel").asText());
//                        placeTile(row, col, label);
//                    } else{
//                        placeTile(row, col);
//                    }
//
//                    //Remove tile from currentPlayer
//                    List<Tile> tiles = currentPlayer.getTiles();
//                    Iterator<Tile> tileIterator = tiles.iterator();
//                    while(tileIterator.hasNext()){
//                        Tile tile = tileIterator.next();
//                        if(tile.getRow() == row && tile.getBoardColumn() == col){
//                            tileIterator.remove();
//                        }
//                    }
//
//                    currentPlayer.setGetRepTile(true);
//                }
//            }
//
//            //2. Buy Shares
//            JsonNode hotelJSON = Action.get("hotel");
//            if (!Objects.equals(hotelJSON.asText(), "[]")){
//                String hotelString = hotelJSON.asText();
//                hotelString = hotelString.substring(1, hotelString.length()-1); // to remove '[' and ']' in the string
//                String[] hotelLabelStrings = hotelString.split(",");
//
//                List<HotelLabel> hotel = new ArrayList<>();
//                for (String hotelLabelString : hotelLabelStrings) {
//                    hotelLabelString = hotelLabelString.trim();// remove any leading/trailing spaces
//                    //System.out.println("Purchased a share of "+hotelLabelString);
//                    HotelLabel hotelLabel = HotelLabel.valueOf(hotelLabelString);
//                    hotel.add(hotelLabel);
//                }
//                if (!hotel.isEmpty()){
//                    buyStock(hotel);
//                }
//            }
//
//            AcquireGameHelper.printBoard(gameState.getBoard());
//
//            if (AcquireGameHelper.checkEndGameCondition(gameState.getBoard())){ //If Game is over
//                break;
//            }
//
//            //3. End Turn
//            endTurn();
//            if (haveAllPlayersRunOutOfTiles() || areAllPlayersTilesUnplayable()){
//                System.out.println("Game Over. Both the banker and all the players have run out of playable tiles!");
//                break;
//            }
//
//        }
//
//        presentWinners(calculateTotalValuation());
//
//        return mapper.valueToTree(gameState);
//    }

    public JsonNode startGame1(JsonNode playersNode){
        setStrat(playersNode);
        gameTree = new GameTree(gameState, banker);
        InternalState IS = new InternalState(gameState, banker);
        currentNode = new Node(null, IS);
        gameTree.generateTree(currentNode);

        while(!AcquireGameHelper.checkEndGameCondition(gameState.getBoard()) ){
            Player currentPlayer = gameState.getCurrentPlayer(); //While game is not over
            System.out.println("Player "+currentPlayer.getPlayer()+"'s turn:");
            System.out.println(currentPlayer.getCash());
            System.out.println(currentPlayer.getShares());
            System.out.println("All tiles: "+currentPlayer.getTiles());
            System.out.println("Children size: "+currentNode.getChildren().size());

            currentNode = turn1();
            System.out.println(currentNode.getChoice());
            InternalState newIS = currentNode.getInternalState();
            State newState = newIS.getState();
            Banker newBanker = newIS.getBanker();
            gameState = newState;
            banker = newBanker;

            AcquireGameHelper.printBoard(gameState.getBoard());
            if (AcquireGameHelper.checkEndGameCondition(gameState.getBoard())){ //If Game is over
                break;
            }
            if (haveAllPlayersRunOutOfTiles() || areAllPlayersTilesUnplayable()){
                System.out.println("Game Over. Both the banker and all the players have run out of playable tiles!");
                break;
            }
            gameState.nextTurn();
            gameTree.generateTree(currentNode);
        }

        presentWinners(calculateTotalValuation());

        return mapper.valueToTree(gameState);
    }

    public Player getWinningPlayer() {
        HashMap<Player, Long> valuations = calculateTotalValuation();
        return Collections.max(valuations.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public void presentWinners(HashMap<Player, Long> playerVals){
        Player winner = null;
        long highestValuation = 0;

        System.out.println("Banker's Tiles: "+ banker.getTilePool());
        for (Map.Entry<Player, Long> entry : playerVals.entrySet()) {
            Player player = entry.getKey();
            Long valuation = entry.getValue();

            List<Tile> Playable = player.getTiles().stream() .filter(tile -> AcquireGameHelper.canPlace(gameState.getBoard(), tile)).collect(Collectors.toList());
            List<Tile> tempUnplayable = player.getTiles().stream() .filter(tile -> AcquireGameHelper.isTempUnplayable(gameState.getBoard(), tile)).collect(Collectors.toList());
            List<Tile> permUnplayable = player.getTiles().stream() .filter(tile -> AcquireGameHelper.isPermUnplayable(gameState.getBoard(), tile)).collect(Collectors.toList());


            System.out.println("Player: " + player.getPlayer() +", Playable: "+ Playable+", perm: "+ permUnplayable+", temp: "+ tempUnplayable+", Valuation: " + valuation);

            if (valuation > highestValuation) {
                winner = player;
                highestValuation = valuation;
            }
        }

        if (winner != null) {
            System.out.println("\nWinner: " + winner.getPlayer() + ", Highest Valuation: " + highestValuation);
        }

    }

    public boolean simulateStateWin(JsonNode Place, List <HotelLabel> hotel){
        //Clones of State, Banker, and Admin
        State simState = State.copyState(gameState);
        Banker simBanker = new Banker(banker);
        AcquireGameAdministratorImpl simAdmin = new AcquireGameAdministratorImpl(simState, simBanker);

        //Place Tile in simulated State
        Player currPlayer = simState.getCurrentPlayer();
        if (Place != null){
            BoardRow row = BoardRow.valueOf(Place.get("row").asText());
            BoardColumn col = BoardColumn.valueOf(Place.get("column").asText());
            List<Tile> tiles = currPlayer.getTiles();
            if (Place.has("hotel")){
                HotelLabel label = HotelLabel.valueOf(Place.get("hotel").asText());
                simAdmin.placeTile(row, col, label);
            } else{
                simAdmin.placeTile(row, col);
            }

            Iterator<Tile> tileIterator = tiles.iterator();
            while(tileIterator.hasNext()){
                Tile tile = tileIterator.next();
                if(tile.getRow() == row && tile.getBoardColumn() == col){
                    tileIterator.remove();
                }
            }
            currPlayer.setGetRepTile(true);

        }

        //Buy Shares in simulated state
        simAdmin.buyStock(hotel);

        //Check if game is over + currPlayer has won
        if (AcquireGameHelper.checkEndGameCondition(simState.getBoard()) || simAdmin.haveAllPlayersRunOutOfTiles()){
            Map<Player, Long> valuation = simAdmin.calculateTotalValuation();
            Map.Entry<Player, Long> maxEntry = null;

            for (Map.Entry<Player, Long> entry : valuation.entrySet()) {
                if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                    maxEntry = entry;
                }
            }

            if (currPlayer.equals(maxEntry.getKey())) {
                return true;
            } else {
                return false;
            }
        }else{
           return false;
        }
    }

    public JsonNode turn(){
        Player currPlayer = gameState.getCurrentPlayer();
        JsonNode Place = null;
        if (!currPlayer.getTiles().isEmpty()){
            Place = currPlayer.placeT(gameState);
        }
        List<HotelLabel> hotel = currPlayer.buyS(banker, gameState);
        boolean win = simulateStateWin(Place, hotel);

        return AcquireGameHelper.createActionJsonNode(win, Place, hotel);

    }

    public Node turn1(){
        Player currPlayer = gameState.getCurrentPlayer();
        List<Node> possibleMoves = currentNode.getChildren();
        List<Node> filteredMoves = currPlayer.filteredMoves(possibleMoves);
        //System.out.println("Filtered Nodes: "+filteredMoves);

        Node matchingNode;
        boolean allPlaceRequests = filteredMoves.stream()
                .allMatch(n -> n.getChoice().has("place"));
        if (allPlaceRequests && filteredMoves.size() > 1) {
            Tile repTile = banker.getRandomTile();
            do {
                // Find the corresponding node. The do-while loop will ensure that this code is executed until a playable tile is found.
                Tile finalRepTile = repTile;
                matchingNode = filteredMoves.stream()
                        .filter(n -> n.getChoice().has("replace")) // Consider only nodes with 'replace' request
                        .filter(n -> {
                            JsonNode replaceNode = n.getChoice().get("replace");
                            // Check if 'replace' request from node matches with repTile row and column
                            return BoardRow.valueOf(replaceNode.get("row").asText()).equals(finalRepTile.getRow())
                                    && BoardColumn.valueOf(replaceNode.get("column").asText()).equals(finalRepTile.getBoardColumn());
                        })
                        .findFirst().orElse(null);  // get the first matching node, if any

                if (matchingNode != null && AcquireGameHelper.isPermUnplayable(matchingNode.getInternalState().getState().getBoard(), repTile)) {
                    repTile = banker.getRandomTile();
                }
            } while (matchingNode != null && AcquireGameHelper.isPermUnplayable(matchingNode.getInternalState().getState().getBoard(), repTile));
        }else{
            matchingNode = filteredMoves.get(0);
        }
        return matchingNode;

    }

    public void resetGame(){
        State.resetGameState();
        Banker.destroyBanker();
    }

}

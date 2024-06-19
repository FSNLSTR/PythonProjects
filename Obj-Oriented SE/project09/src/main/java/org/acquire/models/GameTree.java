package org.acquire.models;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameAdministratorImpl;
import org.acquire.controller.AcquireGameEngineImpl;
import org.acquire.controller.AcquireGameHelper;

import java.util.*;
import java.util.stream.Collectors;

public class GameTree {

    private Node root;
    AcquireGameAdministratorImpl admin;
    private final AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
    public GameTree(State state, Banker banker) {
        root = new Node(null, new InternalState(state, banker));
    }

    public void generateTree(Node node) {
        InternalState originalIS = node.getInternalState();
        State originalState = originalIS.getState();
        Banker originalBanker = originalIS.getBanker();
        Player originalPlayer = originalState.getCurrentPlayer();

        List<Tile> playableTiles = AcquireGameHelper.tilePrint(originalPlayer.getTiles(), originalState);
        if (!playableTiles.isEmpty()) {
            for (Tile placeTile : playableTiles) {
                for (Tile repTile : originalBanker.getTilePool()) {

                    //System.out.println("With replacement tile: "+repTile);
                    State midState = State.copyState(originalState);
                    Banker midBanker = new Banker(originalBanker);
                    Player midPlayer = midState.getCurrentPlayer();
                    admin = new AcquireGameAdministratorImpl(midState, midBanker);

                    BoardRow row = placeTile.getRow();
                    BoardColumn col = placeTile.getBoardColumn();

                    //Place tile
                    JsonNode queryResult = gameEngine.query(midState.getBoard(), row, col);
                    if(queryResult.has("impossible") || queryResult.has("error")){
                        //Tile can only be placed as a founding or a merge, first check if neighbors are singletons
                        List<Tile> foundTiles = AcquireGameHelper.findSuitableTiles(midState.getBoard().getBoardTiles(), row.ordinal(), col.ordinal());
                        Set <HotelLabel> neighborHotelLabels = AcquireGameHelper.findNeighboringHotels(midState.getBoard().getBoardTiles(), row.ordinal(), col.ordinal(), midState.getBoard());

                        //Founding
                        if (!foundTiles.isEmpty() && (neighborHotelLabels == null || neighborHotelLabels.isEmpty())) {

                            //Finding the first unused hotel
                            List<Hotel> hotelList = midState.getBoard().getBoardHotel();
                            HotelLabel label = null;
                            for (HotelLabel hotelLabel : HotelLabel.values()) {
                                boolean found = false;
                                for(Hotel hotel : hotelList){
                                    if(hotel.getLabel().equals(hotelLabel)){
                                        found = true;
                                        break;
                                    }
                                }
                                if(!found) {
                                    label = hotelLabel;
                                    break;
                                }
                            }
                            if (label != null) {
                                admin.placeTile(row, col, label);
                            }
                        }
                        //Merge
                        else if (neighborHotelLabels != null) {
                            //Merge
                            List<Hotel> hotels = midState.getBoard().getBoardHotel();
                            List<Hotel> mostTilesHotels = new ArrayList<>();

                            int maxTiles = -1;
                            for (HotelLabel h : neighborHotelLabels) {
                                for (Hotel hotel : hotels) {
                                    if (h.equals(hotel.getLabel())) {
                                        int currentSize = hotel.getTiles().size();

                                        if (maxTiles < currentSize) {
                                            maxTiles = currentSize;

                                            mostTilesHotels.clear();
                                            mostTilesHotels.add(hotel);

                                        } else if (maxTiles == currentSize) {
                                            mostTilesHotels.add(hotel);
                                        }
                                    }
                                }
                            }

                            // mostTilesHotels now contains the Hotel(s) with most tiles
                            admin.placeTile(row, col, mostTilesHotels.get(0).getLabel());
                        }
                    }else {
                        //Singleton, Growing
                        admin.placeTile(row, col);
                    }

                    //Remove tile from currentPlayer
                    List<Tile> tiles = midPlayer.getTiles();
                    Iterator<Tile> tileIterator = tiles.iterator();
                    while(tileIterator.hasNext()){
                        Tile tile = tileIterator.next();
                        if(tile.getRow() == row && tile.getBoardColumn() == col){
                            tileIterator.remove();
                        }
                    }
                    midPlayer.setGetRepTile(true);

                    //Replacing tile
                    BoardRow rowRep = repTile.getRow();
                    BoardColumn colRep = repTile.getBoardColumn();
                    midPlayer.getTiles().add(new Tile(rowRep, colRep));
                    midPlayer.setGetRepTile(false);
                    midBanker.getTilePool().remove(new Tile(rowRep, colRep));
                    List<List<HotelLabel>> combinations = new ArrayList<>();
                    combinations.add(new ArrayList<>());

                    //Buy shares
                    if (!midState.getBoard().getHotels().isEmpty()){
                        // Get hotels that have shares available and that the player can afford at least one share
                        List<HotelLabel> availableHotels = new ArrayList<>();
                        for (Hotel hotel : midState.getBoard().getHotels()) {
                            int shareCount = midBanker.getCurrentHotelShareCount(hotel.getLabel());
                            int certificatePrice = midBanker.getCertificatePrice(hotel.getLabel(), hotel.getTiles().size());
                            if ((shareCount > 0) && (midPlayer.getCash() >= certificatePrice)) {
                                availableHotels.add(hotel.getLabel());
                            }
                        }

                        combinations = getCombinations(availableHotels, midPlayer.getCash(), midBanker, midState);

                    }
                    for (List<HotelLabel> combination : combinations) {
                        State finalState = State.copyState(midState);
                        Banker finalBanker = new Banker(midBanker);
                        if (!combination.isEmpty()){
                            //System.out.println("Attempting share combination: "+combination);
                            admin = new AcquireGameAdministratorImpl(finalState, finalBanker);
                            admin.buyStock(combination);
                        }
                        midBanker = new Banker(midBanker);
                        Node childNode = new Node(createActionNode(placeTile, repTile, combination), new InternalState(finalState, finalBanker));
                        node.addChild(childNode);
                    }
                }
            }
        }else{
            //Buy shares
            // Get hotels that have shares available and that the player can afford at least one share
            List<HotelLabel> availableHotels = new ArrayList<>();
            for (Hotel hotel : originalState.getBoard().getHotels()) {
                if (originalBanker.getCurrentHotelShareCount(hotel.getLabel()) > 0 && originalPlayer.getCash() >= originalBanker.getCertificatePrice(hotel.getLabel(), hotel.getTiles().size())) {
                    availableHotels.add(hotel.getLabel());
                }
            }

            List<List<HotelLabel>> combinations = getCombinations(availableHotels, originalPlayer.getCash(), originalBanker, originalState);
            for (List<HotelLabel> combination : combinations) {
                State finalState = State.copyState(originalState);
                Banker finalBanker = new Banker(originalBanker);
                if (!combination.isEmpty()){
                    admin = new AcquireGameAdministratorImpl(finalState, finalBanker);
                    admin.buyStock(combination);
                }
                Node childNode = new Node(createActionNode(null, null, combination), new InternalState(finalState, finalBanker));
                node.addChild(childNode);
            }
        }
        System.out.println("Tree has been populated for "+node);
    }

    private List<List<HotelLabel>> getCombinations(List<HotelLabel> availableHotels, long cash, Banker banker, State state) {
        int maxShares = 3;
        final int MAX_HOTEL_LABELS = 3;
        List<List<HotelLabel>> combinations = new ArrayList<>();

        // Loop over all subsets of availableHotels
        for (int i = 0; i < Math.pow((maxShares + 1), availableHotels.size()); i++) {
            List<HotelLabel> combination = new LinkedList<>();
            long totalCost = 0;
            int tempI = i;

            // Loop over all elements in the set
            for (HotelLabel hotelLabel : availableHotels) {
                // Calculate the number of shares to be bought for the current hotel
                int sharesToBuy = tempI % (maxShares + 1);
                tempI = tempI / (maxShares + 1);
                for (int j = 0; j < sharesToBuy; j++) {
                    if (combination.size() == MAX_HOTEL_LABELS) {
                        // Skip the current subset if we've hit the limit
                        break;
                    }
                    combination.add(hotelLabel);
                    totalCost += banker.getCertificatePrice(hotelLabel, state.getBoard().getHotelSize(hotelLabel));

                    // Skip the current subset if we run out of cash
                    if (cash < totalCost) break;
                }
                // Skip the current subset if we run out of cash or hit the limit
                if (cash < totalCost || combination.size() == MAX_HOTEL_LABELS) break;
            }

            // If the player can afford the combination and it does not exceed the limit, add it to the list of combinations
            if (cash >= totalCost && combination.size() <= MAX_HOTEL_LABELS) {
                combinations.add(combination);
            }
        }
        return combinations;
    }


    private JsonNode createActionNode(Tile placeTile, Tile repTile, List<HotelLabel> purchase) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode action = mapper.createObjectNode();

        if (placeTile != null) {
            action.put("place", AcquireGameHelper.createPlaceJsonNode(placeTile.getRow(), placeTile.getBoardColumn(), null));
        }
        if (repTile != null) {
            action.put("replace", AcquireGameHelper.createPlaceJsonNode(repTile.getRow(), repTile.getBoardColumn(), null));
        }
        action.put("hotel", purchase.toString());

        return action;
    }



}

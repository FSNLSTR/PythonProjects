package org.acquire.models;
import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameEngineImpl;
import org.acquire.controller.AcquireGameHelper;

import java.util.*;
import java.util.stream.Collectors;


public class smallestAntiStrategy implements IStrategy{
    private final AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

    String strategy = "";
    public smallestAntiStrategy(){
        this.strategy = "smallest anti";
    }

    public String getStrategy(){
        return this.strategy;
    }

    @Override
    public String toString() {
        return this.strategy;
    }

    @Override
    public JsonNode placeT(Player currPlayer, State gameState){
        Player Oplayer = currPlayer;
        List<Tile> tiles = Oplayer.getTiles();

        List<Tile> playableTiles = AcquireGameHelper.tilePrint(tiles, gameState);

        // 2. Find the smallest tile from the collected tiles
        Tile smallestTile = playableTiles.stream()
                .min(Comparator.comparing(Tile::getRow)
                        .thenComparing(Tile::getBoardColumn))
                .orElse(null);

        if (smallestTile != null) {
            System.out.println("Smallest playable Tile selected: " + smallestTile);
        } else {
            System.out.println("All of the player's tiles are unplayable as they lead to the founding of an 8th chain");
            return null;
        }

        BoardRow row = smallestTile.getRow();
        BoardColumn col = smallestTile.getBoardColumn();

        JsonNode queryResult = gameEngine.query(gameState.getBoard(), row, col);
        System.out.println(queryResult);
        if(queryResult.has("impossible") || queryResult.has("error")){

            // Tile can only be placed as a founding or a merge, first check if neighbors are singletons
            List<Tile> foundTiles = AcquireGameHelper.findSuitableTiles(gameState.getBoard().getBoardTiles(), row.ordinal(), col.ordinal());
            Set <HotelLabel> neighborHotelLabels = AcquireGameHelper.findNeighboringHotels(gameState.getBoard().getBoardTiles(), row.ordinal(), col.ordinal(), gameState.getBoard());

            System.out.println("Found Singletons: "+foundTiles);
            System.out.println("Found Neighbors: "+neighborHotelLabels);

            // If only singletons and no chains around tile, found a new chain
            if (!foundTiles.isEmpty() && (neighborHotelLabels == null || neighborHotelLabels.isEmpty())) {
                //Founding

                // Finding the first unused hotel
                List<Hotel> hotelList = gameState.getBoard().getBoardHotel();
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
                    return AcquireGameHelper.createPlaceJsonNode(row, col, label);
                }
                // If all hotels have been founded, do nothing and tile just ends up being discarded.
            }
            // Merge all singletons and unsafe chains around tile
            else if (neighborHotelLabels != null)
            {
                List<Hotel> hotels = gameState.getBoard().getBoardHotel();
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
                return AcquireGameHelper.createPlaceJsonNode(row, col, mostTilesHotels.get(0).getLabel());

            }
        }else{
            return AcquireGameHelper.createPlaceJsonNode(row, col, null);
        }
        return null;
    }


    @Override
    public List<HotelLabel> buyS(Banker banker, State gameState) {
        List<Hotel> availableHotels = gameState.getBoard().getHotels();

        //Sort available Hotels in reverse alphabetical order
        availableHotels.sort(new Comparator<Hotel>() {
            public int compare(Hotel h1, Hotel h2) {
                return h2.getLabel().toString().compareTo(h1.getLabel().toString()); // swap h1 and h2
            }
        });

        long cash = gameState.getCurrentPlayer().getCash();

        List<HotelLabel> buyList = new ArrayList<>();
        for(Hotel h: availableHotels){
            int cps = banker.getCertificatePrice(h.getLabel(), h.getTiles().size());
            int availableShares = banker.getCurrentHotelShareCount(h.getLabel());

            // Skip hotels with CPS greater than available cash
            if (cps > cash) {
                continue;
            }

            // Calculate the number of shares we can buy with available cash
            int affordableShares = (int) (cash / cps);

            // Buy as many shares as possible while shares are available
            int buyingShares = Math.min(availableShares, affordableShares);
            for (int i = 0; i < buyingShares && buyList.size() < 3; i++) {
                buyList.add(h.getLabel());
                cash -= cps; // reduce the remaining cash
                if (cash < cps) { // break the loop if not enough cash for next share
                    break;
                }
            }

            // Stop buying if we have already bought 3 shares
            if (buyList.size() >= 3) {
                break;
            }
        }
        return buyList;
    }

    @Override
    public List<Node> filterNode(List<Node> possibleMoves){
        List<Node> filteredNodes = AcquireGameHelper.filterSmallestTile(possibleMoves);
        List<Node> finalNodes = AcquireGameHelper.filterAntiAlphabeticalShares(filteredNodes);
        return finalNodes;
    }

}

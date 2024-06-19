package org.acquire.models;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameAdministratorImpl;
import org.acquire.controller.AcquireGameEngineImpl;
import org.acquire.controller.AcquireGameHelper;

import java.util.*;
import java.util.stream.Collectors;

public class RandomStrategy implements IStrategy{

    String strategy = "";
    public RandomStrategy(){
        this.strategy = "random";
    }

    public String getStrategy(){
        return this.strategy;
    }

    @Override
    public String toString() {
        return this.strategy;
    }

    private final AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

    @Override
    public JsonNode placeT(Player currPlayer, State gameState) {
        Player Rplayer = currPlayer;
        List<Tile> tiles = Rplayer.getTiles();
        // Generates a list of playable Tiles in the player's possession
        List<Tile> playableTiles = AcquireGameHelper.tilePrint(tiles, gameState);

        Random random = new Random();
        Tile randomTile = null;

        // If there are playable tiles, select one randomly
        if (!playableTiles.isEmpty()) {
            int randomIndex = random.nextInt(playableTiles.size());
            randomTile = playableTiles.get(randomIndex);
        }

        if (randomTile != null) {
            System.out.println("Random playable Tile selected: " + randomTile);
        } else {
            System.out.println("All of the player's tiles are unplayable as they lead to the founding of an 8th chain");
            return null;
        }

        BoardRow row = randomTile.getRow();
        BoardColumn col = randomTile.getBoardColumn();

        JsonNode queryResult = gameEngine.query(gameState.getBoard(), row, col);
        if(queryResult.has("impossible") || queryResult.has("error")){
            //Tile can only be placed as a founding or a merge, first check if neighbors are singletons
            List <Tile> foundTiles = AcquireGameHelper.findSuitableTiles(gameState.getBoard().getBoardTiles(), row.ordinal(), col.ordinal());
            Set <HotelLabel> neighborHotelLabels = AcquireGameHelper.findNeighboringHotels(gameState.getBoard().getBoardTiles(), row.ordinal(), col.ordinal(), gameState.getBoard());

            System.out.println("Found Singletons: "+foundTiles);
            System.out.println("Found Neighbors: "+neighborHotelLabels);

            // If only singletons and no chains around tile, found a new chain
            if (!foundTiles.isEmpty() && (neighborHotelLabels == null || neighborHotelLabels.isEmpty())) {
                //Founding

                //Finding the first unused hotel
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
                if (label!=null){
                    return AcquireGameHelper.createPlaceJsonNode(row, col, label);
                }
                //In case all chains have been founded, just discard tile

            }
            else if (neighborHotelLabels != null)
            {
                //Merge
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
                if (mostTilesHotels.size() == 1){
                    return AcquireGameHelper.createPlaceJsonNode(row, col, mostTilesHotels.get(0).getLabel());
                }
                else{
                    //NO ANSWER ON WHAT TO DO IF MULTIPLE/ALL CHAINS HAVE THE SAME SIZE SO RANDOMLY CHOOSE ONE TO MERGE
                    System.out.println(mostTilesHotels);
                    int randomInd = random.nextInt(mostTilesHotels.size());
                    return AcquireGameHelper.createPlaceJsonNode(row, col, mostTilesHotels.get(randomInd).getLabel());
                }
            }
        }else{
            return AcquireGameHelper.createPlaceJsonNode(row, col, null);
        }
        return null;
    }

    @Override
    public List<HotelLabel> buyS(Banker banker, State gameState) {
        // if we use only affordable shares
        //pick random num 0-3
        //if 0 break;
        //choose random hotel of affordable shares
        //buy until you reach random num or don't have enough cash for random share
        //Complex: but could find a way to check other available shares, to see if any can be purchased
        //with remaining cash, until random num of shares is hit (1-3)
        Random random = new Random();
        int randomShareCount = random.nextInt(3) + 1; //add one to get between 1-3 shares

        List<Hotel> availableHotels = new ArrayList<>(gameState.getBoard().getHotels());
        long cash = gameState.getCurrentPlayer().getCash();

        List<HotelLabel> buyList = new ArrayList<>();
        for(int i = 0; i < randomShareCount; i++){
            final long cashForFilter = cash;
            //filters out hotels too expensive and ones with not enough shares to purchase.
            availableHotels.removeIf(hotel -> banker.getCertificatePrice(hotel.getLabel(), hotel.getTiles().size()) > cashForFilter || banker.getCurrentHotelShareCount(hotel.getLabel()) < 1);

            if (availableHotels.isEmpty()){
                break;
            }
            int hotelIndex = random.nextInt(availableHotels.size());
            Hotel randomHotel = availableHotels.get(hotelIndex);
            int availableShares = banker.getCurrentHotelShareCount(randomHotel.getLabel());
            if (availableShares < 1){
                continue; //go to next iteration if not enough shares, trivial as they should be filtered before
            }

            long shareCost = banker.getCertificatePrice(randomHotel.getLabel(), randomHotel.getTiles().size());

            cash -= shareCost;
            availableShares -= 1;

            buyList.add(randomHotel.getLabel());

            if (cash < shareCost){
                break;
            }
        }
        return buyList;
    }

    @Override
    public List<Node> filterNode(List<Node> possibleMoves){
        //Choose a random node
        Random random = new Random();
        int randomIndex = random.nextInt(possibleMoves.size());
        Node randomNode = possibleMoves.get(randomIndex);

        List<Node> finalNodes = new ArrayList<>();
        finalNodes.add(randomNode);

        return finalNodes;
    }

}

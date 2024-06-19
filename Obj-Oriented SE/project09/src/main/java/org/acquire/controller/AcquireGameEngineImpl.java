package org.acquire.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.models.Board;
import org.acquire.models.Hotel;
import org.acquire.models.Tile;

import java.util.*;
import java.util.stream.Collectors;

public class AcquireGameEngineImpl implements IAcquireGameEngine {

    // A flag used to handle queries requests.
    public static boolean queryHandler = false;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public JsonNode founding(Board board, BoardRow row, BoardColumn col, HotelLabel hotelLabel) {
        //System.out.println("Attempting to found a hotel... (row: " + row + ", col: " + col + ")");
        if (AcquireGameHelper.checkEndGameCondition(board) || AcquireGameHelper.hotelAlreadyExists(board, hotelLabel)) {
            //System.out.println("Cannot found a hotel. End game condition or hotel already exists.");
            return AcquireGameHelper.createErrorJson("Cannot found a hotel. End game condition or hotel already exists.");
        }

        Tile[][] boardTiles = board.getBoardTiles();
        int rowOrdinal = row.ordinal();
        int colOrdinal = col.ordinal();

//        System.out.println("Before founding process:");
//        AcquireGameHelper.printBoard(board);

        List<Tile> foundTiles = AcquireGameHelper.findSuitableTiles(boardTiles, rowOrdinal, colOrdinal);

        if (!foundTiles.isEmpty()) {
            if (queryHandler == true){
                return AcquireGameHelper.createSuccessJson("founding");
            }
            Tile originalTile = new Tile(row, col);
            boardTiles[row.ordinal()][col.ordinal()] = originalTile;
            //System.out.println("Found a suitable tile. Updating board and hotel...");
            AcquireGameHelper.updateBoardAndHotel(board, foundTiles, originalTile, hotelLabel);
//            System.out.println("After founding process:");
//            AcquireGameHelper.printBoard(board);
            //System.out.println("Founded "+ hotelLabel+" using "+row.toString()+col.toString());
            return mapper.valueToTree(board);
        } else {
            //System.out.println("No suitable tile found for founding a hotel.");
            return AcquireGameHelper.createImpossibleJson("No suitable tile found for founding a hotel.");
        }
    }

    @Override
    public JsonNode merging(Board board, BoardRow row, BoardColumn col, HotelLabel hotelLabel) {
        Hotel mergerHotel = board.getBoardHotel().stream().filter(hotel -> hotel.getLabel()==hotelLabel).findFirst().orElse(null);
        if(mergerHotel==null){
            return AcquireGameHelper.createImpossibleJson("Provided hotel:"+hotelLabel.toString()+" does not yet founded");
        }
        List<Tile> adjacentTiles = AcquireGameHelper.getAdjacentTiles(board.getBoardTiles(), row.ordinal(), col.ordinal());

        //if no hotel chain found adjacent to provided co-ordinates with the given label
        if(!adjacentTiles.stream().anyMatch(tile -> tile.getHotelLabel()==hotelLabel)){
            return AcquireGameHelper.createImpossibleJson("No associated hotel chain:"+hotelLabel.toString()+ " found horizontally/vertically to the provided row, col:"+"(%d,%d)".formatted(row.ordinal(),col.ordinal()));

        }

        //omitting the tiles that are empty, belong to the provided hotel chain
        List<Tile> filteredAdjacentTiles = adjacentTiles
                .stream().filter(tile -> tile.getHotelLabel()!=null && tile.getHotelLabel()!=hotelLabel)
                .collect(Collectors.toList());
        if(filteredAdjacentTiles.size()==0){
            return AcquireGameHelper.createErrorJson("No valid hotel found to merge");

        }
//        Predicate<Tile> validAcquiredStarterTileFilterFunction = new Predicate<Tile>() {
//            @Override
//            public boolean test(Tile tile) {
//                Hotel h = board.getBoardHotel().stream().filter(hotel -> hotel.getTiles().)
//            }
//        }

        Boolean safeHotels = filteredAdjacentTiles.stream()
                .anyMatch(tile -> board.getBoardHotel().stream().anyMatch(
                        hotel -> hotel.getLabel()==tile.getHotelLabel() && hotel.getTiles().size()>=11
                ));
        if(safeHotels){
            return AcquireGameHelper.createImpossibleJson("Atleast one hotel adjacent to tile placement is safe.");
        }
        List<Hotel> validAcquiredHotels = filteredAdjacentTiles.stream()
                .map(tile->board.getBoardHotel().stream().filter(hotel -> hotel.getLabel()==tile.getHotelLabel() &&
                        hotel.getTiles().size()<=mergerHotel.getTiles().size()).findFirst().get())
                .filter(hotel -> hotel!=null)
                .collect(Collectors.toList());
        if(validAcquiredHotels.size()==0){
            return AcquireGameHelper.createImpossibleJson("All adjacent hotels are safe");
        }

        HashSet<HotelLabel> needToRemoveHotels = new HashSet<>();
        validAcquiredHotels.forEach(hotel -> {
            if(!queryHandler){
                List<Tile> tiles = hotel.getTiles();
                for(Tile tile:tiles){
                    HotelLabel newLabel = mergerHotel.getLabel();
                    tile.setHotelLabel(newLabel);
                    for (Tile t: board.getTiles()){
                        if ((t.getRow().equals(tile.getRow())) && (t.getBoardColumn().equals(tile.getBoardColumn()))){
                            t.setHotelLabel(newLabel);
                        }
                    }
                }
                mergerHotel.getTiles().addAll(hotel.getTiles());
            }
            needToRemoveHotels.add(hotel.getLabel());
        });

        Map<Object,Object> response = new HashMap<>();
        response.put("acquirer", mergerHotel.getLabel().toString());
        response.put("acquired", needToRemoveHotels.stream().map(hotelLabelLambds -> hotelLabelLambds.toString()).collect(Collectors.toList()));
        JsonNode responseNode = AcquireGameHelper.createJsonFromMap(response);

        if(!queryHandler){
            board.setBoardHotel(
                    board.getBoardHotel().stream()
                            .filter(hotel -> !needToRemoveHotels.contains(hotel.getLabel()))
                            .collect(Collectors.toList())
            );
            Tile addedTile = new Tile(row,col);
            addedTile.setHotelLabel(mergerHotel.getLabel());
            mergerHotel.getTiles().add(addedTile);
            Tile[][] tiles = board.getBoardTiles();
            tiles[row.ordinal()][col.ordinal()] = addedTile;

            List<Tile> adjacentSingletons = adjacentTiles.stream()
                    .filter(tile -> tile.getHotelLabel() == null)
                    .collect(Collectors.toList());

            if (!adjacentSingletons.isEmpty()){
                //System.out.println("Adjacent Singletons: "+ adjacentSingletons);
                for (Tile adjacentTile : adjacentSingletons) {
                    adjacentTile.setHotelLabel(hotelLabel);
                    List<Tile> hotelTiles = mergerHotel.getTiles();
                    hotelTiles.add(adjacentTile);
                    mergerHotel.setTiles(hotelTiles);

                }
            }
            if (!AcquireGameHelper.isBoardValid(board)){
                System.out.println("Board isn't valid");
            }

            return mapper.valueToTree(board);
        }

//        AcquireGameHelper.printBoard(board);
        // discuss checking "end game" condition usage

        return responseNode;
    }


    @Override
    public JsonNode singleton(Board board, BoardRow row, BoardColumn col) {
        if(AcquireGameHelper.checkEndGameCondition(board)) {
            return AcquireGameHelper.createErrorJson("Game is over. Cannot place a tile.");
        }
        if(row == null || col == null) {
            return AcquireGameHelper.createErrorJson("Invalid row or column.");
        }
        Tile[][] boardTiles = board.getBoardTiles();

        int rowOrdinal = row.ordinal();
        int colOrdinal = col.ordinal();
        if(rowOrdinal >= boardTiles.length || colOrdinal >= boardTiles[0].length) {
            return AcquireGameHelper.createErrorJson("Row or column out of bounds.");
        }

        // Check if the specified tile position is already occupied
        if (boardTiles[rowOrdinal][colOrdinal] != null) {
            return AcquireGameHelper.createImpossibleJson("Tile position already occupied.");
        }

        if(AcquireGameHelper.hasAdjacentTiles(boardTiles,rowOrdinal,colOrdinal)){
            return AcquireGameHelper.createImpossibleJson("Can not place a singleton tile with adjacent tiles when more chains can be founded");
        }

        if (queryHandler == true){
            return AcquireGameHelper.createSuccessJson("singleton");
        }

        boardTiles[rowOrdinal][colOrdinal] = new Tile(row, col);

        board.setBoardTiles(boardTiles);

        //System.out.println("Placed a singleton at "+row.toString()+col.toString());
        return mapper.valueToTree(board);
    }

    @Override
    public JsonNode growing(Board board, BoardRow row, BoardColumn col) {
//        System.out.println("Before growing process:");
//        AcquireGameHelper.printBoard(board);
        if(AcquireGameHelper.checkEndGameCondition(board)) {
            return AcquireGameHelper.createErrorJson("Game is over. Cannot grow a hotel.");
        }
        //System.out.println("Attempting to grow a hotel... (row: " + row + ", col: " + col + ")");

        Tile[][] boardTiles = board.getBoardTiles();
        int rowOrdinal = row.ordinal();
        int colOrdinal = col.ordinal();

        if (!AcquireGameHelper.isValidTileForGrowing(boardTiles, rowOrdinal, colOrdinal)) {
            //System.out.println("Invalid tile for growing.");
            return AcquireGameHelper.createErrorJson("Invalid tile for growing.");
        }

        Hotel hotelObjToGrow = AcquireGameHelper.findNeighboringHotelOfTile(boardTiles, rowOrdinal, colOrdinal, board);
        if (hotelObjToGrow != null) {
            Map<Object, Object> response = new HashMap<>();
            response.put("growing", hotelObjToGrow);

            if(queryHandler == true){
                return AcquireGameHelper.createJsonFromMap(response);
            }
            //System.out.println("Neighboring tile found: " + hotelObjToGrow);

            Tile originalTile = new Tile(row, col);
            boardTiles[row.ordinal()][col.ordinal()] = originalTile;


            //Look for singletons around original tile that would also join the chain
            List<Tile> neighboringSingletons = AcquireGameHelper.getAdjacentTiles(boardTiles, rowOrdinal, colOrdinal);
            neighboringSingletons.removeIf(tile -> tile.getHotelLabel() != null);

            originalTile.setHotelLabel(hotelObjToGrow.getLabel());
            List<Tile> existingHotelTiles =  hotelObjToGrow.getTiles();
            existingHotelTiles.add(originalTile);
            if (!neighboringSingletons.isEmpty()){
                for(Tile nS :neighboringSingletons){
                    nS.setHotelLabel(hotelObjToGrow.getLabel());
                    existingHotelTiles.add(nS);
                }
            }
//            System.out.println("After growing process:");
//            AcquireGameHelper.printBoard(board);
            //System.out.println("Grew "+hotelObjToGrow.getLabel()+" using "+row.toString()+col.toString());
            return mapper.valueToTree(board);
        } else {
            //System.out.println("No neighboring tile found for growing.");
            return AcquireGameHelper.createErrorJson("No neighboring tile found for growing.");
        }
    }

    @Override
    public JsonNode query(Board board, BoardRow row, BoardColumn col) {
        if(AcquireGameHelper.checkEndGameCondition(board)) {
            return AcquireGameHelper.createErrorJson("Game is over. Cannot make a query.");
        }
        queryHandler = true;
        // Get the labels that are not already on the board


        List<JsonNode> response = new ArrayList<>();

        // Call the growing method
        JsonNode growingResult = growing(board, row, col);
        if (growingResult.has("growing")) {
            queryHandler = false;
            return growingResult;
        }

        // Call the singleton method
        JsonNode singletonResult = singleton(board, row, col);
        if (singletonResult.has("singleton")) {
            queryHandler = false;
            return singletonResult;
        }

        // Call the founding method for each available label

        queryHandler = false;
        return AcquireGameHelper.createImpossibleJson("Merging not possible");


    }

    public JsonNode query(Board board, BoardRow row, BoardColumn col,HotelLabel label){
        Set<HotelLabel> existingLabels = board.getBoardHotel().stream()
                .map(Hotel::getLabel)
                .collect(Collectors.toSet());

        Set<HotelLabel> availableLabels = new HashSet<>(Arrays.asList(HotelLabel.values()));
        availableLabels.removeAll(existingLabels);
        queryHandler = true;
        List<JsonNode> foundingSuccessResponse = new ArrayList<>();
        List<JsonNode> foundingErrorResponse = new ArrayList<>();


        JsonNode foundingResult = founding(board, row, col, label);

        if (foundingResult != null && foundingResult.has("founding")) {
            queryHandler = false;

            return foundingResult;
        }
//        if (foundingResult.has("error") || foundingResult.has("impossible")) {
//            queryHandler = false;
//
//            return AcquireGameHelper.createErrorJson("founding : " + foundingResult.asText());
//        }
        if (foundingResult.has("impossible")) {
            queryHandler = false;

            return AcquireGameHelper.createErrorJson("founding : " + foundingResult.asText());
        }

        // If no successful founding, add a single error message for founding
        //System.out.println("Trying merge...");

        JsonNode mergingResult = merging(board, row, col, label);

        if (mergingResult != null && mergingResult.has("acquirer")) {
            queryHandler = false;
            return mergingResult;

        }

        queryHandler = false;
        //System.out.println("Merge not possible");
        return AcquireGameHelper.createImpossibleJson("Merging not possible");
    }

}

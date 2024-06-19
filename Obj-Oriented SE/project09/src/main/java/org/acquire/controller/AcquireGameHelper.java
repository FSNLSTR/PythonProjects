package org.acquire.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.models.*;

import java.util.*;
import java.util.stream.Collectors;

public class AcquireGameHelper {

    private static final JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
    public static final int[] x = {0, 0, 1, -1};
    public static final int[] y = {1, -1, 0, 0};
    public static boolean checkEndGameCondition(Board board) {
        if(board.getBoardHotel().size()==0){
            return false;
        }
        for (Hotel hotel : board.getBoardHotel()) {
            if (hotel.getTiles().size() >= 41) {
                System.out.println("Game over! Hotel chain " + hotel.getLabel() + " has 41 tiles or more.");
                return true;
            }
        }
        if (areAllHotelChainsSafe(board)) {
            System.out.println("Game over! All hotel chains are safe.");
            return true;
        }
        return false;
    }

    private static boolean areAllHotelChainsSafe(Board board) {

        for (Hotel hotel : board.getBoardHotel()) {
            if (!isHotelChainSafe(board, hotel)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isHotelChainSafe(Board board, Hotel hotel) {
        return hotel.getTiles().size() >= 11;
    }

    private static boolean doesPlayerHaveTiles(Board board, Player player){ return !player.getTiles().isEmpty(); }


//    public static Tile findSuitableTile(Tile[][] boardTiles, int row, int col) {
//        System.out.println("Finding a suitable tile for founding the hotel... (row: " + row + ", col: " + col + ")");
//        List<Tile> neighbors = AcquireGameHelper.getAdjacentTiles(boardTiles, row, col);
//
//        for (Tile neighbor : neighbors) {
//            System.out.println("Checking neighbor: " + neighbor);
//            if (neighbor != null &&
//                    !hasOtherNeighbors(boardTiles, neighbor.getRow().ordinal(), neighbor.getBoardColumn().ordinal(), null) &&
//                    !hasOtherNeighbors(boardTiles, neighbor.getRow().ordinal(), neighbor.getBoardColumn().ordinal(), boardTiles[row][col])) {
//                System.out.println("Suitable tile found: " + neighbor);
//                return neighbor;
//            }
//        }
//
//        System.out.println("No suitable tile found.");
//        return null;
//    }

    public static List<Tile> findSuitableTiles(Tile[][] boardTiles, int row, int col) {
        //System.out.println("Finding suitable tiles for ... (row: " + row + ", col: " + col + ")");
        List<Tile> neighbors = AcquireGameHelper.getAdjacentTiles(boardTiles, row, col);
        List<Tile> suitableTiles = new ArrayList<>();

        for (Tile neighbor : neighbors) {
            //System.out.println("Checking neighbor: " + neighbor);
            if (neighbor != null &&
                    !hasOtherNeighbors(boardTiles, neighbor.getRow().ordinal(), neighbor.getBoardColumn().ordinal(), null) &&
                    !hasOtherNeighbors(boardTiles, neighbor.getRow().ordinal(), neighbor.getBoardColumn().ordinal(), boardTiles[row][col])) {
                //System.out.println("Suitable tile found: " + neighbor);
                suitableTiles.add(neighbor);
            }
        }

        if(suitableTiles.isEmpty()) {
            //System.out.println("No suitable tiles found.");
        }
        return suitableTiles;
    }
    public static boolean hotelAlreadyExists(Board board, HotelLabel hotelLabel) {
        return board.getBoardHotel().stream().anyMatch(hotel -> hotel.getLabel() == hotelLabel);
    }

    public static List<Tile> getAdjacentTiles(Tile[][] boardTiles, int row, int col) {
        List<Tile> neighbors = new LinkedList<>();

        for (int i = 0; i < 4; i++) {
            int newRow = row + x[i];
            int newCol = col + y[i];

            if (isValidTile(newRow, newCol, boardTiles.length, boardTiles[0].length) && boardTiles[newRow][newCol]!=null) {
                neighbors.add(boardTiles[newRow][newCol]);
            }
        }

        return neighbors;
    }

    public static boolean hasAdjacentTiles(Tile[][] boardTiles, int row, int col) {
        return getAdjacentTiles(boardTiles, row, col).stream().anyMatch(tile -> tile != null);
    }

    public static boolean isValidTile(int row, int col, int numRows, int numCols) {
        return isValidCoordinate(row, col, numRows, numCols);
    }

    public static boolean isValidCoordinate(int row, int col, int numRows, int numCols) {
        return row >= 0 && row < numRows && col >= 0 && col < numCols;
    }

    public static boolean hasOtherNeighbors(Tile[][] boardTiles, int row, int col, Tile excludedTile) {
        List<Tile> neighbors = getAdjacentTiles(boardTiles, row, col);

        for (Tile neighbor : neighbors) {
            // Check if the neighbor is not null before invoking equals
            if (neighbor != null && !neighbor.equals(excludedTile) && hasAdjacentTiles(boardTiles, neighbor.getRow().ordinal(), neighbor.getBoardColumn().ordinal())) {
                return true;
            }
        }

        return false;
    }

public static void updateBoardAndHotel(Board board, List<Tile> foundTiles, Tile originalTile, HotelLabel hotelLabel) {
        // Update the found tile with the hotel label
        List<Tile> tiles = new LinkedList<>();
        for (Tile foundTile : foundTiles) {
            foundTile.setHotelLabel(hotelLabel);
            tiles.add(foundTile);
        }
        // Create a new hotel with the found tile and the original tile (if not null)

        if (originalTile != null) {
            originalTile.setHotelLabel(hotelLabel);
            tiles.add(originalTile);
        }

        Hotel newHotel = new Hotel(hotelLabel, tiles);

        // Add the new hotel to the board's list of hotels
        List<Hotel> updatedHotels = new LinkedList<>(board.getBoardHotel());
        updatedHotels.add(newHotel);
        board.setBoardHotel(updatedHotels);
    }


    public static void printBoard(Board board) {
        System.out.println("Board:");
        Tile[][] boardTiles = board.getBoardTiles();

        // Define ANSI color codes
        String[] colors = new String[]{
                "\033[0;31m",  // Red
                "\033[0;32m",  // Green
                "\033[0;33m",  // Yellow
                "\033[0;34m",  // Blue
                "\033[0;35m",   // Magenta
                "\033[0;36m",   // Cyan
                "\033[38;5;198m"   // Hot pink
        };
        HashMap<HotelLabel, String> colorMap = new HashMap<>();
        int colorIndex = 0;

        for (Hotel hotel : board.getBoardHotel()) {
            if (!colorMap.containsKey(hotel.getLabel())) {
                colorMap.put(hotel.getLabel(), colors[colorIndex]);
                colorIndex = (colorIndex+1) % colors.length;
            }
        }

        // Print column headers
        System.out.print("\t ");
        for (int i = 1; i <= boardTiles[0].length; i++) {
            System.out.printf("%-8s", i);
        }
        System.out.println();

        char rowHeader = 'A';
        for (Tile[] boardTile : boardTiles) {
            System.out.printf("%-4s", rowHeader++);
            for (Tile tile : boardTile) {
                if (tile == null) {
                    System.out.printf("%-8s", "null");
                } else if (tile.getHotelLabel() == null) {
                    String rowCol = tile.getRow() + tile.getColumn();
                    System.out.printf("%-8s", rowCol);
                } else {
                    String color = colorMap.get(tile.getHotelLabel());
                    System.out.printf(color + "%-8s" + "\033[0m", tile.getHotelLabel().toString().substring(0, 4));
                }
            }
            System.out.println();
        }

        // Print all Hotels and their Tiles
        System.out.println("Hotels: (" + board.getHotels().size() + ")");
        for (Hotel hotel : board.getBoardHotel()) {
            String color = colorMap.get(hotel.getLabel());
            System.out.println("Hotel: " + color + hotel.getLabel() + "\033[0m (" + hotel.getTiles().size() + ")");
            for (Tile tile : hotel.getTiles()) {
                System.out.println("  Tile: " + tile.getRow() + tile.getColumn() + " - Hotel: " + color + tile.getHotelLabel() + "\033[0m");
            }
        }

        System.out.println("------------------------------");
    }

    public static boolean isValidTileForGrowing(Tile[][] boardTiles, int row, int col) {
        // Check if the tile is not a part of any hotel chain
        return boardTiles[row][col] == null;
    }

    public static Hotel findNeighboringHotelOfTile(Tile[][] boardTiles, int row, int col, Board board) {
        List<Tile> neighbors = getAdjacentTiles(boardTiles, row, col);
        Set<HotelLabel> neighborHotelLabels = new HashSet<>();

        Iterator<Tile> iterator = neighbors.iterator();
        while (iterator.hasNext()) {
            Tile neighbor = iterator.next();
            //System.out.println("Labels: " + (neighbor != null ? neighbor.getHotelLabel() : null));

            if (neighbor != null) {
                if (neighbor.getHotelLabel() != null) {
                    neighborHotelLabels.add(neighbor.getHotelLabel());
                }
            } else {
                iterator.remove();
            }
        }
        if (neighbors.size() == 0){
            //System.out.println("Error : No Valid Neighbors Found");
            return null;
        }
        // Check if all neighbors belong to the same hotel chain
        if (neighborHotelLabels.size() == 1) {
            //System.out.println("Neighbor : "+neighbors.get(0));
            for (Tile neighbor : neighbors) {
                for (Hotel hotel : board.getBoardHotel()) {
                    if (hotel.getLabel() == neighborHotelLabels.iterator().next()) {
                        return hotel;
                    }
                }
            }
            return null;
        } else {
            //System.out.println("Error: Neighbors should belong to the same hotel chain for growing.");
            return null;
        }
    }

    public static Set<HotelLabel> findNeighboringHotels(Tile[][] boardTiles, int row, int col, Board board){
        List<Tile> neighbors = getAdjacentTiles(boardTiles, row, col);
        Set<HotelLabel> neighborHotelLabels = new HashSet<>();

        Iterator<Tile> iterator = neighbors.iterator();
        while (iterator.hasNext()) {
            Tile neighbor = iterator.next();
            //System.out.println("Labels: " + (neighbor != null ? neighbor.getHotelLabel() : null));

            if (neighbor != null) {
                if (neighbor.getHotelLabel() != null) {
                    neighborHotelLabels.add(neighbor.getHotelLabel());
                }
            } else {
                iterator.remove();
            }
        }
        if (neighbors.size() == 0){
            //System.out.println("Error : No Valid Neighbors Found");
            return null;
        }
        return neighborHotelLabels;
    }
    public static boolean isTempUnplayable(Board board, Tile tile){
        //Identify if the placement of this tile on this board would create an eighth chain (i.e temporarily unplayable)
        List<Tile> foundTiles = findSuitableTiles(board.getBoardTiles(), tile.getRow().ordinal(), tile.getBoardColumn().ordinal());
        Set<HotelLabel> neighbors = findNeighboringHotels(board.getBoardTiles(), tile.getRow().ordinal(), tile.getBoardColumn().ordinal(), board);
        if ((board.getHotels().size() == 7) && (!foundTiles.isEmpty()) && (neighbors == null || neighbors.isEmpty())){
            return true;
        }
        return false;
    }

    public static boolean isPermUnplayable(Board board, Tile tile){
        //Identify if the placement of this tile on this board would attempt to merge 2 or more safe neighboring chains.
        //If there are more than on chains next to the tile, check if placing the tile would merge two or more chains when all but one of the chains contain 11 or more tile
        Set<HotelLabel> neighboringHotels = findNeighboringHotels(board.getBoardTiles(), tile.getRow().ordinal(), tile.getBoardColumn().ordinal(), board);

        if (neighboringHotels != null){
            if (neighboringHotels.size() > 1){
                // There are more than one neighboring chains next to the tile
                // Check if placing the tile will merge two or more chains when all but one of the chains contain 11 or more tiles
                int chainsOfSizeElevenOrMore = 0;
                for(HotelLabel hotel : neighboringHotels) {
                    if(board.getHotelSize(hotel) >= 11) {
                        chainsOfSizeElevenOrMore++;
                    }
                }
                if(chainsOfSizeElevenOrMore >=2) {
                    // This placement would merge multiple chains of 11 or more tiles, so return true
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Tile> tilePrint(List<Tile> tiles, State gameState){
        List<Tile> playableTiles = tiles.stream().filter(tile -> AcquireGameHelper.canPlace(gameState.getBoard(), tile)).collect(Collectors.toList());
        List<Tile> tempUnplayable = tiles.stream().filter(tile -> AcquireGameHelper.isTempUnplayable(gameState.getBoard(), tile)).collect(Collectors.toList());
        List<Tile> permUnplayable = tiles.stream().filter(tile -> AcquireGameHelper.isPermUnplayable(gameState.getBoard(), tile)).collect(Collectors.toList());

        // Print out playable and unplayable tiles
        System.out.println("P Tiles: "+playableTiles);
        if (!tempUnplayable.isEmpty()){
            System.out.println("TU Tiles: "+tempUnplayable);
        }
        if (!permUnplayable.isEmpty()){
            System.out.println("PU Tiles: "+permUnplayable);
        }

        return playableTiles;
    }

    public static boolean canPlace(Board board, Tile tile) {
        return !isTempUnplayable(board, tile) && !isPermUnplayable(board, tile);
    }

    public static JsonNode createJsonFromArray(List<JsonNode> elements) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        arrayNode.addAll(elements);
        return arrayNode;
    }


    public static JsonNode createSuccessJson(String requestType) {
        ObjectNode successNode = jsonFactory.objectNode();
        successNode.put(requestType, requestType);
        return successNode;
    }

    public static JsonNode createErrorJson(String errorMessage) {
        ObjectNode errorNode = jsonFactory.objectNode();
        errorNode.put("error", errorMessage);
        return errorNode;
    }

    public static JsonNode createJsonFromMap(Map<Object,Object> map) {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.valueToTree(map);
    }

    public static JsonNode createImpossibleJson(String s) {
        ObjectNode errorNode = jsonFactory.objectNode();
        errorNode.put("impossible", s);
        return errorNode;
    }

    public static boolean checkPlayerHasTile(Player player,Tile tile){
        for(Tile tile1: player.getTiles()){
            boolean fl = tile1.equals(tile);
            if(fl){
                return true;
            }
        }

        return false;
    }

    public static JsonNode createPlaceJsonNode(BoardRow row, BoardColumn column, HotelLabel label) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode place = mapper.createObjectNode();
        if (row != null) {
            place.put("row", row.toString());
        }
        if (column != null) {
            place.put("column", column.toString());
        }
        if (label != null) {
            place.put("hotel", label.toString());
        }
        return place;
    }

    //Need to modify to include win
    public static JsonNode createActionJsonNode(boolean win, JsonNode Place, List<HotelLabel> Hotel){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode action = mapper.createObjectNode();
        action.put("win", win);
        action.put("hotel", Hotel.toString());
        if (Place != null){
            action.put("place", Place);
        }
        return action;
    }

    public static List<Node> filterSmallestTile(List<Node> possibleMoves){
        // Step 1: Find the minimum 'row' value among all nodes
        BoardRow minRow = possibleMoves.stream()
                .map(n -> BoardRow.valueOf(n.getChoice().get("place").get("row").asText()))
                .min(Comparator.comparing(BoardRow::ordinal))
                .orElse(null);

        // Step 2: Among the nodes with the minimum 'row', find the ones with the smallest 'column'
        List<Node> filteredNodes;

        if (minRow != null) {
            // Get nodes which are in the smallest row
            List<Node> nodesInSmallestRow = possibleMoves.stream()
                    .filter(n -> BoardRow.valueOf(n.getChoice().get("place").get("row").asText()) == minRow)
                    .collect(Collectors.toList());

            // Find the smallest column in these nodes
            BoardColumn minColumn = nodesInSmallestRow.stream()
                    .map(n -> BoardColumn.valueOf(n.getChoice().get("place").get("column").asText()))
                    .min(Comparator.comparing(BoardColumn::ordinal))
                    .orElse(null);

            // Get the nodes corresponding to the smallest row and column
            if (minColumn != null) {
                filteredNodes = nodesInSmallestRow.stream()
                        .filter(n -> BoardColumn.valueOf(n.getChoice().get("place").get("column").asText()) == minColumn)
                        .collect(Collectors.toList());
            } else {
                filteredNodes = new ArrayList<>();
            }
        } else {
            filteredNodes = new ArrayList<>();
        }
        return filteredNodes;

    }

    public static List<Node> filterLargestTile(List<Node> possibleMoves){
        // Step 1: Find the maximum 'row' value among all nodes
        BoardRow maxRow = possibleMoves.stream()
                .map(n -> BoardRow.valueOf(n.getChoice().get("place").get("row").asText()))
                .max(Comparator.comparing(BoardRow::ordinal))
                .orElse(null);

        // Step 2: Among the nodes with the maximum 'row', find the ones with the largest 'column'
        List<Node> filteredNodes;

        if (maxRow != null) {
            // Get nodes which are in the largest row
            List<Node> nodesInLargestRow = possibleMoves.stream()
                    .filter(n -> BoardRow.valueOf(n.getChoice().get("place").get("row").asText()) == maxRow)
                    .collect(Collectors.toList());

            // Find the largest column in these nodes
            BoardColumn maxColumn = nodesInLargestRow.stream()
                    .map(n -> BoardColumn.valueOf(n.getChoice().get("place").get("column").asText()))
                    .max(Comparator.comparing(BoardColumn::ordinal))
                    .orElse(null);

            // Get the nodes corresponding to the largest row and column
            if (maxColumn != null) {
                filteredNodes = nodesInLargestRow.stream()
                        .filter(n -> BoardColumn.valueOf(n.getChoice().get("place").get("column").asText()) == maxColumn)
                        .collect(Collectors.toList());
            } else {
                filteredNodes = new ArrayList<>();
            }
        } else {
            filteredNodes = new ArrayList<>();
        }
        return filteredNodes;
    }

    public static List<Node> filterAlphabeticalShares(List<Node> possibleMoves) {
        // Mapper for converting JSON to List<HotelLabel>
        ObjectMapper objectMapper = new ObjectMapper();
        // Hotels in reverse alphabetical order

        // Generate hotel scores - each is worth (3*prev) + 1
        Map<HotelLabel, Integer> hotelScores = new HashMap<>();
        hotelScores.put(HotelLabel.valueOf("Worldwide"), 1);
        hotelScores.put(HotelLabel.valueOf("Tower"), 4);
        hotelScores.put(HotelLabel.valueOf("Sackson"), 13);
        hotelScores.put(HotelLabel.valueOf("Imperial"), 40);
        hotelScores.put(HotelLabel.valueOf("Festival"), 121);
        hotelScores.put(HotelLabel.valueOf("Continental"), 364);
        hotelScores.put(HotelLabel.valueOf("American"), 1093);

        System.out.println("Hotel Scores"+hotelScores);

        List<HotelLabel> bestShares = new ArrayList<>();
        int highestScore = 0;
        boolean anyHotelFound = false;

        for (Node node : possibleMoves) {
            JsonNode hotelsNode = node.getChoice().get("hotel");
            if (hotelsNode != null && !Objects.equals(hotelsNode.asText(), "[]")) {
                String hotelString = hotelsNode.asText();
                hotelString = hotelString.substring(1, hotelString.length()-1); // to remove '[' and ']' in the string
                String[] hotelLabelStrings = hotelString.split(",");

                int addScore = 0;
                List<HotelLabel> hotel = new ArrayList<>();
                for (String hotelLabelString : hotelLabelStrings) {
                    hotelLabelString = hotelLabelString.trim();// remove any leading/trailing spaces
                    HotelLabel hotelLabel = HotelLabel.valueOf(hotelLabelString);
                    addScore += hotelScores.get(hotelLabel);
                    hotel.add(hotelLabel);
                }

                if (addScore > highestScore){
                    bestShares = hotel;
                    highestScore = addScore;
                }

                // Skip nodes with no hotels
                if (hotel.isEmpty()) continue;
                anyHotelFound = true;

            }

        }
        final List<HotelLabel> bestSharesFinal = bestShares;
        if (anyHotelFound) {
            return possibleMoves.stream()
                    .filter(node -> {
                        JsonNode hotelsNode = node.getChoice().get("hotel");
                        if (hotelsNode == null || Objects.equals(hotelsNode.asText(), "[]")) {
                            return false;
                        }

                        String hotelString = hotelsNode.asText();
                        hotelString = hotelString.substring(1, hotelString.length() - 1); // remove '[' and ']' in the string
                        String[] hotelLabelStrings = hotelString.split(",");

                        List<HotelLabel> nodeHotelList = new ArrayList<>();
                        for (String hotelLabelString : hotelLabelStrings) {
                            hotelLabelString = hotelLabelString.trim(); // remove any leading/trailing spaces
                            HotelLabel hotelLabel = HotelLabel.valueOf(hotelLabelString);
                            nodeHotelList.add(hotelLabel);
                        }

                        return nodeHotelList.equals(bestSharesFinal);
                    })
                    .collect(Collectors.toList());
        } else {
            return possibleMoves;
        }
    }

    public static List<Node> filterAntiAlphabeticalShares(List<Node> possibleMoves) {
        // Mapper for converting JSON to List<HotelLabel>
        ObjectMapper objectMapper = new ObjectMapper();
        // Hotels in reverse alphabetical order

        // Generate hotel scores
        Map<HotelLabel, Integer> hotelScores = new HashMap<>();
        hotelScores.put(HotelLabel.valueOf("American"), 1);
        hotelScores.put(HotelLabel.valueOf("Continental"), 4);
        hotelScores.put(HotelLabel.valueOf("Festival"), 13);
        hotelScores.put(HotelLabel.valueOf("Imperial"), 40);
        hotelScores.put(HotelLabel.valueOf("Sackson"), 121);
        hotelScores.put(HotelLabel.valueOf("Tower"), 364);
        hotelScores.put(HotelLabel.valueOf("Worldwide"), 1093);

        System.out.println("Hotel Scores"+hotelScores);

        List<HotelLabel> bestShares = new ArrayList<>();
        int highestScore = 0;
        boolean anyHotelFound = false;

        for (Node node : possibleMoves) {
            JsonNode hotelsNode = node.getChoice().get("hotel");
            if (hotelsNode != null && !Objects.equals(hotelsNode.asText(), "[]")) {
                String hotelString = hotelsNode.asText();
                hotelString = hotelString.substring(1, hotelString.length()-1); // to remove '[' and ']' in the string
                String[] hotelLabelStrings = hotelString.split(",");

                int addScore = 0;
                List<HotelLabel> hotel = new ArrayList<>();
                for (String hotelLabelString : hotelLabelStrings) {
                    hotelLabelString = hotelLabelString.trim();// remove any leading/trailing spaces
                    HotelLabel hotelLabel = HotelLabel.valueOf(hotelLabelString);
                    addScore += hotelScores.get(hotelLabel);
                    hotel.add(hotelLabel);
                }

                if (addScore > highestScore){
                    bestShares = hotel;
                    highestScore = addScore;
                }

                // Skip nodes with no hotels
                if (hotel.isEmpty()) continue;
                anyHotelFound = true;

            }

        }
        final List<HotelLabel> bestSharesFinal = bestShares;
        if (anyHotelFound) {
            return possibleMoves.stream()
                    .filter(node -> {
                        JsonNode hotelsNode = node.getChoice().get("hotel");
                        if (hotelsNode == null || Objects.equals(hotelsNode.asText(), "[]")) {
                            return false;
                        }

                        String hotelString = hotelsNode.asText();
                        hotelString = hotelString.substring(1, hotelString.length() - 1); // remove '[' and ']' in the string
                        String[] hotelLabelStrings = hotelString.split(",");

                        List<HotelLabel> nodeHotelList = new ArrayList<>();
                        for (String hotelLabelString : hotelLabelStrings) {
                            hotelLabelString = hotelLabelString.trim(); // remove any leading/trailing spaces
                            HotelLabel hotelLabel = HotelLabel.valueOf(hotelLabelString);
                            nodeHotelList.add(hotelLabel);
                        }

                        return nodeHotelList.equals(bestSharesFinal);
                    })
                    .collect(Collectors.toList());
        } else {
            return possibleMoves;
        }
    }

    public static boolean isBoardValid(Board board) {
        Tile[][] boardTiles = board.getBoardTiles();

        for (int i = 0; i < boardTiles.length; i++) {
            for (int j = 0; j < boardTiles[i].length; j++) {
                Tile tile = boardTiles[i][j];

                if (tile != null && tile.getHotelLabel() != null) {
                    HotelLabel tileHotelLabel = tile.getHotelLabel();
                    boolean doesTileHotelExist = false;

                    // Find the hotel associated with this tile
                    for (Hotel hotel : board.getHotels()) {
                        if (hotel.getLabel().equals(tileHotelLabel)) {
                            doesTileHotelExist = true;

                            if (!hotel.getTiles().contains(tile)) {
                                // The hotel's tiles doesn't contain this tile
                                return false;
                            }
                        }
                    }

                    if (!doesTileHotelExist) {
                        // There is no hotel associated with this tile
                        return false;
                    }
                }
            }
        }

        for (Hotel hotel : board.getHotels()) {
            for (Tile tile : hotel.getTiles()) {
                Tile boardTile = boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()];

                if (boardTile == null || !boardTile.getHotelLabel().equals(hotel.getLabel())) {
                    // A tile contained in a hotel is not present on the board or doesn't have the right hotel label
                    return false;
                }
            }
        }

        return true;
    }


}

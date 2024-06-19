package test.acquire.unit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameEngineImpl;
import org.acquire.controller.AcquireGameHelper;
import org.acquire.models.Board;
import org.acquire.models.Hotel;
import org.acquire.models.Tile;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class AcquireGameEngineImplTest {
    @Test
    public void testGameQueryWithFounding() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        AcquireGameEngineImpl.queryHandler = true;

        JsonNode foundingResult = gameEngine.founding(board, BoardRow.D, BoardColumn._10, HotelLabel.Worldwide);
        System.out.println(foundingResult);
        AcquireGameEngineImpl.queryHandler = false;
        assertTrue(foundingResult.has("founding"));
    }

    @Test
    public void testGameQueryWithMerging(){
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9),
                tile4 = new Tile(BoardRow.C,BoardColumn._2),
                tile5 = new Tile(BoardRow.C,BoardColumn._3);

        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles1 = new ArrayList<>(),
                hotelTiles2 = new ArrayList<>();
        hotelTiles1.add(tile1);
        hotelTiles1.add(tile2);
        for(Tile tile:hotelTiles1){
            tile.setHotelLabel(HotelLabel.American);
        }
        hotelTiles2.add(tile4);
        hotelTiles2.add(tile5);
        for(Tile tile:hotelTiles2){
            tile.setHotelLabel(HotelLabel.Continental);
        }
        Hotel hotel1 = new Hotel(HotelLabel.American, hotelTiles1),
                hotel2 = new Hotel(HotelLabel.Continental,hotelTiles2);

        List<Tile> tiles = new ArrayList<>(Arrays.asList(new Tile[]{tile1,tile2,tile3,tile4,tile5}));
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel1);
        hotels.add(hotel2);
        Board board = new Board(boardTiles,hotels);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        AcquireGameEngineImpl.queryHandler = true;

        JsonNode foundingResult = gameEngine.merging(board, BoardRow.C, BoardColumn._1, HotelLabel.American);
        System.out.println(foundingResult);
        AcquireGameEngineImpl.queryHandler = false;
        gameEngine.merging(board, BoardRow.C, BoardColumn._1, HotelLabel.American);
        assertEquals(5, hotel1.getTiles().size());
    }

    @Test
    public void testGameQueryWithGrowing() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        AcquireGameEngineImpl.queryHandler = true;

        JsonNode foundingResult = gameEngine.growing(board, BoardRow.C, BoardColumn._1);
        System.out.println(foundingResult);
        AcquireGameEngineImpl.queryHandler = false;
        assertTrue(foundingResult.has("growing"));
    }

    @Test
    public void testGameQueryWithSingleton() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        AcquireGameEngineImpl.queryHandler = true;

        JsonNode foundingResult = gameEngine.singleton(board, BoardRow.H, BoardColumn._1);
        System.out.println(foundingResult);
        AcquireGameEngineImpl.queryHandler = false;
        assertTrue(foundingResult.has("singleton"));
    }


    @Test
    public void testGameWithSingleton1() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();


        JsonNode foundingResult = gameEngine.singleton(board, BoardRow.C, BoardColumn._1);
        System.out.println(foundingResult);
        assertEquals("Can not place a singleton tile with adjacent tiles when more chains can be founded", foundingResult.path("impossible").asText());
    }


    @Test
    public void testGameWithSingleton2() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();


        JsonNode foundingResult = gameEngine.singleton(board, BoardRow.H, BoardColumn._1);
        System.out.println(foundingResult);
        List<Tile> tilesNew = board.getTiles();
        assertEquals(4, tilesNew.size());
    }


    @Test
    public void testGameWithSingleton3() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();


        JsonNode foundingResult = gameEngine.singleton(board, BoardRow.A, BoardColumn._1);
        System.out.println(foundingResult);
        assertEquals("Tile position already occupied.", foundingResult.path("impossible").asText());

    }

    @Test
    public void testGameWithGrowing1() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

        JsonNode foundingResult = gameEngine.growing(board, BoardRow.C, BoardColumn._1);
        System.out.println(foundingResult);

        List<Hotel> hotelsName = board.getBoardHotel();
        Optional<Hotel> americanHotelOptional = hotelsName.stream()
                .filter(hotelName -> hotelName.getLabel() == HotelLabel.American)
                .findFirst();

        assertTrue(americanHotelOptional.isPresent());
        Hotel americanHotel = americanHotelOptional.get();
        assertEquals(3, americanHotel.getTiles().size());

    }


    @Test
    public void testGameWithGrowing2() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

        JsonNode foundingResult = gameEngine.growing(board, BoardRow.H, BoardColumn._1);
        System.out.println(foundingResult);
        assertEquals("No neighboring tile found for growing.", foundingResult.path("error").asText());

    }

    @Test
    public void testGameWithGrowing3() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

        JsonNode foundingResult = gameEngine.growing(board, BoardRow.A, BoardColumn._1);
        System.out.println(foundingResult);
        assertEquals("Invalid tile for growing.", foundingResult.path("error").asText());

    }


    @Test
    public void testGameWithGrowing4() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.D, BoardColumn._9);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);
        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();

        JsonNode foundingResult = gameEngine.growing(board, BoardRow.D, BoardColumn._10);
        System.out.println(foundingResult);
        assertEquals("No neighboring tile found for growing.", foundingResult.path("error").asText());

    }

    @Test
    public void testGameWithFounding1() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.E, BoardColumn._6);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);

        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        JsonNode foundingResult = gameEngine.founding(board, BoardRow.E, BoardColumn._7, HotelLabel.Worldwide);
        System.out.println(foundingResult);
        assertTrue(foundingResult.path("hotels").toString().contains("Worldwide"));

    }

    @Test
    public void testGameWithFounding2() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.B, BoardColumn._2);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);

        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        JsonNode foundingResult = gameEngine.founding(board, BoardRow.A, BoardColumn._2, HotelLabel.Worldwide);
        System.out.println(foundingResult);
        assertEquals("No suitable tile found for founding a hotel.", foundingResult.path("impossible").asText());

    }


    @Test
    public void testGameWithFounding3() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.B, BoardColumn._3);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);

        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        JsonNode foundingResult = gameEngine.founding(board, BoardRow.A, BoardColumn._2, HotelLabel.Worldwide);
        System.out.println(foundingResult);
        assertEquals("No suitable tile found for founding a hotel.", foundingResult.path("impossible").asText());

    }


    @Test
    public void testGameWithFounding4() {
        Tile tile1 = new Tile(BoardRow.A, BoardColumn._1);
        Tile tile2 = new Tile(BoardRow.B, BoardColumn._1);
        Tile tile3 = new Tile(BoardRow.C, BoardColumn._3);
        Tile[][] boardTiles = new Tile[BoardRow.values().length][BoardColumn.values().length];

        List<Tile> hotelTiles = new ArrayList<>();
        hotelTiles.add(tile1);
        hotelTiles.add(tile2);
        for(Tile tile:hotelTiles){
            tile.setHotelLabel(HotelLabel.American);
        }
        Hotel hotel = new Hotel(HotelLabel.American, hotelTiles);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);
        for(Tile tile:tiles){
            boardTiles[tile.getRow().ordinal()][tile.getBoardColumn().ordinal()] = tile;
        }

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);

        Board board = new Board(boardTiles,hotels);

        AcquireGameHelper.printBoard(board);
        AcquireGameEngineImpl gameEngine = new AcquireGameEngineImpl();
        JsonNode foundingResult = gameEngine.founding(board, BoardRow.C, BoardColumn._4, HotelLabel.American);
        System.out.println(foundingResult);
        assertEquals("Cannot found a hotel. End game condition or hotel already exists.", foundingResult.path("error").asText());

    }

}

package test.acquire.unit.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameAdministratorImpl;
import org.acquire.controller.AcquireGameHelper;
import org.acquire.controller.AcquireGameParser;
import org.acquire.models.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.Silent.class)
public class AcquireGameAdministratorTest {
    private AcquireGameAdministratorImpl gameAdmin;

    private ObjectMapper mapper;

    private JsonNode testNode;

    @Spy
    private Deque<Player> players = new LinkedList<>();

    @Spy
    private List<Hotel> hotels = new LinkedList<>();

    @Spy
    private Board board = new Board();


    @Spy
    private Banker banker = Banker.getBanker();

    private AcquireGameParser parser;

    @Spy
    private Player player1,player2;

    @Spy
    private Hotel hotel1,hotel2;
    private State state;

    @Captor
    ArgumentCaptor<Long> captor;
    @Before
    public void init(){


        when(player1.getPlayer()).thenReturn("player 1");
        when(player2.getPlayer()).thenReturn("player 2");
        when(player1.getCash()).thenReturn(9999l);
        when(player2.getCash()).thenReturn(9999l);

        players.add(player1);
        players.add(player2);
        assertEquals(2, players.size());

        doReturn(0).when(banker).getCurrentHotelShareCount(any());
        state = spy(State.getInstance(board, players));
        // stubbing hotels
        doReturn(HotelLabel.American).when(hotel1).getLabel();
        doReturn(HotelLabel.Continental).when(hotel2).getLabel();

//
        List<Tile> tiles2 = spy(LinkedList.class),
                tiles1 = spy(LinkedList.class);
        hotel1.setTiles(tiles1);
        hotel2.setTiles(tiles2);
        doReturn(11).when(tiles1).size();
        doReturn(45).when(tiles2).size();
        hotels.add(hotel1);
        hotels.add(hotel2);
        board.setBoardHotel(hotels);
        gameAdmin = spy(new AcquireGameAdministratorImpl(state, banker));

    }
    @After
    public void exit(){
        State.resetGameState();
        players.removeAll(players);
        gameAdmin.destroy();

    }
//    @Before
//    public void setUp() throws IOException {
//        mapper = new ObjectMapper();
//        testNode = mapper.readTree(new File("C:\\Users\\Avinash\\Desktop\\Assingments\\OOSD\\Project\\group03\\project05\\src\\test\\resources\\inputJson\\in0.json"));
//        State state = State.parseState(testNode.get("state"));
//        Banker banker = Banker.getBanker();
//        gameAdmin = new AcquireGameAdministratorImpl(state,banker);
//    }

    @Test
    public void testInitiateGame(){
        List<String> initiatePlayer = spy(new LinkedList<>());
        String playerName = "player1";
        initiatePlayer.add(playerName);
        // test regular use case
        try {
            gameAdmin.initiateGame(initiatePlayer);

        }catch (IllegalArgumentException e){
            assertEquals(playerName, state.removeLastPlayer().getPlayer());
        }
        // test if player name is null
        initiatePlayer.add(null);
        assertTrue(gameAdmin.initiateGame(initiatePlayer).has("error"));
        initiatePlayer.removeIf(name->name==null);
        // test if player name is more than 20 chars
        initiatePlayer.add("12345678912345678912345");
        assertTrue(gameAdmin.initiateGame(initiatePlayer).has("error"));
        initiatePlayer = new ArrayList<>();
        initiatePlayer.add(" ");
        assertTrue(gameAdmin.initiateGame(initiatePlayer).has("error"));
        initiatePlayer.add(" t");
        try {
            gameAdmin.initiateGame(initiatePlayer);

        }catch (IllegalArgumentException e){
            assertEquals("t", state.removeLastPlayer().getPlayer());
        }
    }

    @Test
    public void testPlaceTile(){
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
        Player p1 = spy(new Player());
        doReturn("p1").when(p1).getPlayer();
        p1.getTiles().add(new Tile(BoardRow.B,BoardColumn._2));
        doReturn(9999l).when(p1).getCash();

        State state = State.resetGameState(board,new LinkedList<>(Arrays.asList(new Player[]{p1})));
        AcquireGameAdministratorImpl gameAdmin = new AcquireGameAdministratorImpl(state,Banker.getBanker());
        try{
            gameAdmin.placeTile(BoardRow.B, BoardColumn._2).toPrettyString();
        }catch (IllegalArgumentException e){
            assertEquals(3,state.getBoard().getBoardHotel().stream().
                    filter(hotel3 -> hotel3.getLabel()==HotelLabel.American).
                    findAny().get().getTiles().size());
        }
    }

    @Test
    public void testPlaceTileWithLabel(){

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
        Player p1 = spy(new Player());
        doReturn("p1").when(p1).getPlayer();
        p1.getTiles().add(new Tile(BoardRow.D,BoardColumn._10));
        doReturn(9999l).when(p1).getCash();

        State state = State.resetGameState(board,new LinkedList<>(Arrays.asList(new Player[]{p1})));
        AcquireGameAdministratorImpl gameAdmin = new AcquireGameAdministratorImpl(state,Banker.getBanker());
        try{
            gameAdmin.placeTile(BoardRow.D, BoardColumn._10, HotelLabel.Worldwide).toPrettyString();
        }catch (IllegalArgumentException e){
            assertTrue(state.getBoard().getBoardHotel().stream().anyMatch(hotel3 -> hotel3.getLabel()==HotelLabel.Worldwide));
        }
    }

    @Test
    public void testBuy(){
        //testing to buy share for not in in-play hotel
        assertTrue(gameAdmin.buyStock(Arrays.asList(new HotelLabel[]{HotelLabel.Festival})).has("error"));

        //testing to buy share when there are no shares left in the pool
        assertTrue(gameAdmin.buyStock(Arrays.asList(new HotelLabel[]{HotelLabel.Continental})).has("impossible"));

        assertEquals(1200, banker.getCertificatePrice(HotelLabel.Continental,hotel2.getTiles().size()));
         // Valid purchase testing
        doReturn(10).when(banker).getCurrentHotelShareCount(any());

        long initialCash = player1.getCash();

        // Buying shares from an in-play hotel
        try{
            gameAdmin.buyStock(Arrays.asList(new HotelLabel[]{HotelLabel.American}));
        }catch (IllegalArgumentException e){
            reset(player1);
            long finalCash = player1.getCash();

            long sharePrice = banker.getCertificatePrice(HotelLabel.American, hotel1.getTiles().size());
            assertEquals(initialCash - sharePrice, finalCash);
        }
//        assertTrue(gameAdmin.buyStock(Arrays.asList(new HotelLabel[]{HotelLabel.American})).has("success"));

        // Verify that player's cash value gets updated after buying shares

    }
    
    
    @Test
    public void testEndTurn(){
        gameAdmin.endTurn();
        assertEquals(player2.getPlayer(), state.getCurrentPlayer().getPlayer());
        for(int i=0;i<107;i++){
            gameAdmin.endTurn();
        }
        // Checking tile pool exhaustion case
        assertTrue(gameAdmin.endTurn());
    }
}

package test.acquire.unit.models;

import org.acquire.models.Board;
import org.acquire.models.Player;
import org.acquire.models.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Deque;
import java.util.LinkedList;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class StateTest {

    @Spy
    private Deque<Player> players = new LinkedList<>();

    @Spy
    private Board board = new Board();
    @Mock
    private Player player1,player2;


    private State state;
    @Before
    public void init(){


        when(player1.getPlayer()).thenReturn("player 1");
        when(player2.getPlayer()).thenReturn("player 2");
        players.add(player1);
        players.add(player2);
        assertEquals(2, players.size());

        state = spy(State.getInstance(board, players));


    }
    @After
    public void exit(){
        State.resetGameState();
        players.removeAll(players);
    }
    @Test
    public void testGetInstance(){
        assertEquals(state.getCurrentPlayer().getPlayer(), State.getInstance().getCurrentPlayer().getPlayer());


        State.getInstance().resetGameState();
        assertFalse(state.isInitialized());

    }

    @Test
    public void testNextTurn(){
        assertEquals(player1.getPlayer(), players.getFirst().getPlayer());
        state.nextTurn();
        assertEquals(player2.getPlayer(), players.getFirst().getPlayer());

        assertTrue(state.removePlayer(player1.getPlayer()));
        assertEquals(1, players.size());
        assertEquals(player2.getPlayer(), state.getCurrentPlayer().getPlayer());
        state.nextTurn();
        assertEquals(player2.getPlayer(), state.getCurrentPlayer().getPlayer());
    }
}

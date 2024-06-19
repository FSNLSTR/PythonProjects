package org.acquire.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.models.Board;
import org.acquire.models.Tile;

import java.util.ArrayList;
import java.util.List;



public interface IAcquireGameAdministrator {


    JsonNode placeTile(BoardRow row, BoardColumn col);
    JsonNode placeTile(BoardRow row, BoardColumn col, HotelLabel label);
    JsonNode buyStock(List<HotelLabel> hotelLabel);
    boolean endTurn();
    boolean isGameOver();

    JsonNode initiateGame(List<String> players);

}

// Class representing a player

// Class representing the game administrator

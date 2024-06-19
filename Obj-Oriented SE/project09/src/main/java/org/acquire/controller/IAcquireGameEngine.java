package org.acquire.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.models.Board;

public interface IAcquireGameEngine {
    JsonNode founding(Board board, BoardRow row, BoardColumn col, HotelLabel hotelLabel);
    JsonNode merging(Board board, BoardRow row, BoardColumn col, HotelLabel hotelLabel);

    JsonNode singleton(Board board, BoardRow row, BoardColumn col);
    JsonNode growing(Board board, BoardRow row, BoardColumn col);
    JsonNode query(Board board, BoardRow row, BoardColumn col);

}

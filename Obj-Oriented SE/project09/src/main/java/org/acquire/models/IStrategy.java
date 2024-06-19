package org.acquire.models;


import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.HotelLabel;

import java.util.List;

public interface IStrategy {

    String toString();
    JsonNode placeT(Player currPlayer, State gameState);
    List<HotelLabel> buyS(Banker banker, State gameState);

    public List<Node> filterNode(List<Node> possibleMoves);

}

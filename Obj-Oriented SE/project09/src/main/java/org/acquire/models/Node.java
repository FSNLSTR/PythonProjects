package org.acquire.models;

import com.fasterxml.jackson.databind.JsonNode;
import org.acquire.constants.BoardColumn;
import org.acquire.constants.BoardRow;
import org.acquire.constants.HotelLabel;
import org.acquire.controller.AcquireGameEngineImpl;
import org.acquire.controller.AcquireGameHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class Node {

    private JsonNode choice;
    private InternalState internalState;
    private List<Node> children;

    public Node(JsonNode choice, InternalState internalState) {
        this.choice = choice;
        this.internalState = internalState;
        this.children = new ArrayList<>();
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public InternalState getInternalState() {
        return internalState;
    }

    public void setChoice(JsonNode choice){this.choice = choice; }

    public JsonNode getChoice(){
        return this.choice;
    }

    public List<Node> getChildren(){
        return this.children;
    }

    public String toString(){
        return "Node [choice=" + choice +"]";
    }
}

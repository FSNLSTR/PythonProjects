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
public class InternalState {

    public InternalState(State state, Banker banker) {
        this.state = state;
        this.banker = banker;
    }
    private State state;
    private Banker banker;

    public State getState(){
        return state;
    }

    public Banker getBanker(){
        return banker;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setBanker(Banker banker){
        this.banker = banker;
    }


}

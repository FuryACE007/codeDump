package com.tradematching.contracts;

import com.tradematching.states.ERState;
import org.junit.Test;

public class StateTests {

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldOfCorrectType() throws NoSuchFieldException {
        ERState.class.getDeclaredField("msg");
        assert (ERState.class.getDeclaredField("msg").getType().equals(String.class));
    }
}
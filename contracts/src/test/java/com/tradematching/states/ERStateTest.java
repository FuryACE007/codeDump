package com.tradematching.states;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ERStateTest {

    private final Party broker = new TestIdentity(new CordaX500Name("Broker", "London", "GB")).getParty();
    private final Party fidNode = new TestIdentity(new CordaX500Name("FidNode", "New York", "US")).getParty();
    private final int quantity = 10;
    private final int price = 100;

    @Test
    public void testERStateConstructorAndGetters() {
        UUID placementId = UUID.randomUUID();
        UniqueIdentifier erId = new UniqueIdentifier();

        ERState erState = new ERState(placementId, erId, broker, fidNode, quantity, price);

        assertEquals(placementId, erState.getPlacementId());
        assertEquals(erId, erState.getErId());
        assertEquals(broker, erState.getBroker());
        assertEquals(fidNode, erState.getFidNode());
        assertEquals(quantity, erState.getQuantity());
        assertEquals(price, erState.getPrice());
    }

    @Test
    public void testERStateParticipants() {
        UUID placementId = UUID.randomUUID();
        UniqueIdentifier erId = new UniqueIdentifier();

        ERState erState = new ERState(placementId, erId, broker, fidNode, quantity, price);

        assertEquals(2, erState.getParticipants().size());
        assertEquals(broker, erState.getParticipants().get(0));
        assertEquals(fidNode, erState.getParticipants().get(1));
    }
}

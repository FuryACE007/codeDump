package com.tradematching.states;

import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class PlacementSummaryTest {

    private final Party broker = new TestIdentity(new CordaX500Name("Broker", "London", "GB")).getParty();
    private final Party fidNode = new TestIdentity(new CordaX500Name("FidNode", "New York", "US")).getParty();
    private final int cumulativeQuantity = 100;
    private final int cumulativePrice = 5000;
    private final float avgPrice = 50.0f;

    @Test
    public void testPlacementSummaryConstructorAndGetters() {
        UUID placementId = UUID.randomUUID();

        PlacementSummary placementSummary = new PlacementSummary(placementId, broker, fidNode, cumulativeQuantity, cumulativePrice, avgPrice);

        assertEquals(placementId, placementSummary.getPlacementId());
        assertEquals(broker, placementSummary.getBroker());
        assertEquals(fidNode, placementSummary.getFidNode());
        assertEquals(cumulativeQuantity, placementSummary.getCumulativeQuantity());
        assertEquals(cumulativePrice, placementSummary.getCumulativePrice());
        assertEquals(avgPrice, placementSummary.getAvgPrice(), 0.0f);
    }

    @Test
    public void testPlacementSummaryParticipants() {
        UUID placementId = UUID.randomUUID();

        PlacementSummary placementSummary = new PlacementSummary(placementId, broker, fidNode, cumulativeQuantity, cumulativePrice, avgPrice);

        assertEquals(2, placementSummary.getParticipants().size());
        assertEquals(broker, placementSummary.getParticipants().get(0));
        assertEquals(fidNode, placementSummary.getParticipants().get(1));
    }
}

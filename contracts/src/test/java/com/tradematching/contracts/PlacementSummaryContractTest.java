package com.tradematching.contracts;

import com.tradematching.states.PlacementSummary;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class PlacementSummaryContractTest {

    private final MockServices ledgerServices = new MockServices(
            (Iterable<String>) Arrays.asList("com.tradematching.contracts", "com.tradematching.states"),
            new TestIdentity(new CordaX500Name("MockIdentity", "London", "GB"))
    );

    private PlacementSummary validPlacementSummary;
    private PlacementSummary invalidPlacementSummary;

    @Before
    public void setup() {
        Party brokerA = new TestIdentity(new CordaX500Name("BrokerA", "London", "GB")).getParty();
        Party brokerB = new TestIdentity(new CordaX500Name("BrokerB", "New York", "US")).getParty();

        validPlacementSummary = new PlacementSummary(
                UUID.randomUUID(), // placementId
                brokerA, // broker
                brokerB, // fidNode
                100, // cumulativeQuantity
                5000, // cumulativePrice
                50.0f // avgPrice
        );

        invalidPlacementSummary = new PlacementSummary(
                UUID.randomUUID(), // placementId
                brokerB, // broker (invalid)
                brokerA, // fidNode
                100, // cumulativeQuantity
                5000, // cumulativePrice
                50.0f // avgPrice
        );
    }

    @Test
    public void testPlacementSummaryContract_ValidTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(PlacementSummaryContract.ID, validPlacementSummary);
            tx.command(validPlacementSummary.getBroker().getOwningKey(), new PlacementSummaryContract.Commands.GenerateSummary());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void testPlacementSummaryContract_InvalidTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(PlacementSummaryContract.ID, invalidPlacementSummary);
            tx.command(invalidPlacementSummary.getBroker().getOwningKey(), new PlacementSummaryContract.Commands.GenerateSummary());
            tx.fails();
            return null;
        });
    }
}

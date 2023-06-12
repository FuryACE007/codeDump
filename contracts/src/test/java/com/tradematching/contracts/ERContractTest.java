package com.tradematching.contracts;

import com.tradematching.states.ERState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ERContractTest {

    private final MockServices ledgerServices = new MockServices(
            (Iterable<String>) Arrays.asList("com.tradematching.contracts", "com.tradematching.states"),
            new TestIdentity(new CordaX500Name("MockIdentity", "London", "GB"))
    );

    private ERState validERState;
    private ERState invalidERState;

    @Before
    public void setup() {
        Party alice = new TestIdentity(new CordaX500Name("Alice", "London", "GB")).getParty();
        Party bob = new TestIdentity(new CordaX500Name("Bob", "New York", "US")).getParty();

        validERState = new ERState(
                UUID.randomUUID(), // placementId
                new UniqueIdentifier(), // erId
                alice, // broker
                bob, // fidNode
                100, // quantity
                10 // price
        );

        invalidERState = new ERState(
                UUID.randomUUID(), // placementId
                new UniqueIdentifier(), // erId
                alice, // broker
                bob, // fidNode
                0, // quantity (invalid)
                10 // price
        );
    }

    @Test
    public void testERContract_ValidTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(ERContract.ID, validERState);
            tx.command(validERState.getBroker().getOwningKey(), new ERContract.Commands.IssueER());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void testERContract_InvalidTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(ERContract.ID, invalidERState);
            tx.command(invalidERState.getBroker().getOwningKey(), new ERContract.Commands.IssueER());
            tx.fails();
            return null;
        });
    }
}

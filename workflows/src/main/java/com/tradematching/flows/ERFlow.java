package com.tradematching.flows;

import com.tradematching.contracts.ERContract;
import com.tradematching.contracts.PlacementSummaryContract;
import com.tradematching.flows.ERFlow;
import com.tradematching.states.ERState;
import com.tradematching.states.PlacementSummary;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ERFlowTests {
    private MockNetwork network;
    private StartedMockNode brokerNode;
    private StartedMockNode fidNode;
    private Party broker;
    private Party fid;

    @Before
    public void setup() {
        // Create a mock network with two nodes
        network = new MockNetwork(new MockNetworkParameters().withCordappsForPackages("com.tradematching"));

        // Create nodes for broker and fidNode
        brokerNode = network.createPartyNode(new TestIdentity(new CordaX500Name("Broker", "London", "GB")));
        fidNode = network.createPartyNode(new TestIdentity(new CordaX500Name("Fidelity", "New York", "US")));

        // Get party identities
        broker = brokerNode.getInfo().getLegalIdentities().get(0);
        fid = fidNode.getInfo().getLegalIdentities().get(0);
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void flowReturnsSignedTransaction() throws Exception {
        // Create a placement summary state
        PlacementSummary placementSummary = new PlacementSummary(
                UUID.randomUUID(), broker, fid, 10, 100, 10.0f
        );

        // Add the placement summary state to brokerNode's vault
        brokerNode.getServices().getVaultService().verifyAndRegisterState(placementSummary);

        // Create an ER flow initiator
        UUID placementId = placementSummary.getPlacementId();
        int quantity = 5;
        int price = 50;
        ERFlow.ERFlowInitiator flowInitiator = new ERFlow.ERFlowInitiator(placementId, quantity, price);

        // Run the ER flow on brokerNode
        StartedMockNode flowResult = brokerNode.startFlow(flowInitiator).get(0);
        network.runNetwork();

        // Get the signed transaction from the flow result
        SignedTransaction signedTx = flowResult.getServices().getValidatedTransactions().getTransaction(flowResult.getId());

        // Verify the transaction
        signedTx.verifySignaturesExcept(Collections.singletonList(broker.getOwningKey()));

        // Verify the transaction's commands
        List<Command<?>> commands = signedTx.getTx().getCommands();
        assertEquals(2, commands.size());

        Command<?> erCommand = commands.get(0);
        assertEquals(ERContract.Commands.IssueER.class, erCommand.getValue().getClass());
        assertEquals(Arrays.asList(broker.getOwningKey(), fid.getOwningKey()), erCommand.getSigners());

        Command<?> placementSummaryCommand = commands.get(1);
        assertEquals(PlacementSummaryContract.Commands.GenerateSummary.class, placementSummaryCommand.getValue().getClass());
        assertEquals(Arrays.asList(broker.getOwningKey(), fid.getOwningKey()), placementSummaryCommand.getSigners());
    }
}

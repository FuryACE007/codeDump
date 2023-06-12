//package com.tradematching;
//import co.paralleluniverse.fibers.Suspendable;
//import com.tradematching.contracts.ERContract;
//import com.tradematching.contracts.PlacementSummaryContract;
//import com.tradematching.flows.ERFlow;
//import com.tradematching.states.ERState;
//import com.tradematching.states.PlacementSummary;
//import net.corda.core.concurrent.CordaFuture;
//import net.corda.core.contracts.Command;
//import net.corda.core.contracts.CommandData;
//import net.corda.core.contracts.UniqueIdentifier;
//import net.corda.core.crypto.SecureHash;
//import net.corda.core.identity.CordaX500Name;
//import net.corda.core.identity.Party;
//import net.corda.core.node.services.Vault;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.testing.contracts.DummyContract;
//import net.corda.testing.core.TestIdentity;
//import net.corda.testing.node.*;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import static net.corda.testing.core.TestConstants.getDUMMY_NOTARY;
//import static net.corda.testing.core.TestConstants.getALICE;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//public class ERFlowTests {
//
//    private MockNetwork mockNetwork;
//    private StartedMockNode aliceNode;
//    private StartedMockNode bobNode;
//
//    private final Party broker = new TestIdentity(new CordaX500Name("Broker", "London", "GB")).getParty();
//    private final Party fidNode = new TestIdentity(new CordaX500Name("FidNode", "New York", "US")).getParty();
//
//    private final UUID placementId = UUID.randomUUID();
//    private final int quantity = 100;
//    private final int price = 5000;
//
//    @Before
//    public void setup() {
//        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters(
//                Arrays.asList((String) new String[]{"com.tradematching.flows"}, "com.tradematching.contracts", "com.tradematching.states")
//        );
//        mockNetwork = new MockNetwork(mockNetworkParameters);
//
//        aliceNode = mockNetwork.createPartyNode(new CordaX500Name("Alice", "London", "GB"));
//        bobNode = mockNetwork.createPartyNode(new CordaX500Name("Bob", "New York", "US"));
//
//        // Registering the flow responder on the counterparty node
//        bobNode.registerInitiatedFlow(ERFlow.ERFlowResponder.class);
//    }
//
//    @After
//    public void tearDown() {
//        mockNetwork.stopNodes();
//    }
//
//    @Test
//    public void testERFlow() {
//        // Start the ERFlowInitiator flow on Alice's node
//        CordaFuture<SignedTransaction> flow = aliceNode.startFlow(
//                new ERFlow.ERFlowInitiator(placementId, quantity, price)
//        );
//
//        // Run network until all the flows are complete
//        mockNetwork.runNetwork();
//
//        // Get the signed transaction from the initiating flow
//        SignedTransaction signedTransaction = flow.getReturnValue().get();
//
//        // Verify that the transaction is signed by all required parties
//        signedTransaction.verifySignaturesExcept(getDUMMY_NOTARY().getOwningKey());
//
//        // Get a reference to the recorded transaction from both parties' vaults
//        Vault.Page<ERState> aliceERStates = aliceNode.getServices().getVaultService()
//                .queryBy(ERState.class)
//                .getStates();
//        Vault.Page<PlacementSummary> alicePlacementSummaries = aliceNode.getServices().getVaultService()
//                .queryBy(PlacementSummary.class)
//                .getStates();
//        Vault.Page<ERState> bobERStates = bobNode.getServices().getVaultService()
//                .queryBy(ERState.class)
//                .getStates();
//        Vault.Page<PlacementSummary> bobPlacementSummaries = bobNode.getServices().getVaultService()
//                .queryBy(PlacementSummary.class)
//                .getStates();
//
//        // Ensure that Alice and Bob both have the recorded ERState and PlacementSummary states in their vaults
//        assertEquals(1, aliceERStates.getStates().size());
//        assertEquals(1, alicePlacementSummaries.getStates().size());
//        assertEquals(1, bobERStates.getStates().size());
//        assertEquals(1, bobPlacementSummaries.getStates().size());
//
//        // Ensure that Alice and Bob have the same placementId in the ERState and PlacementSummary states
//        assertEquals(placementId, aliceERStates.getStates().get(0).getState().getData().getPlacementId());
//        assertEquals(placementId, alicePlacementSummaries.getStates().get(0).getState().getData().getPlacementId());
//        assertEquals(placementId, bobERStates.getStates().get(0).getState().getData().getPlacementId());
//        assertEquals(placementId, bobPlacementSummaries.getStates().get(0).getState().getData().getPlacementId());
//
//        // Ensure that the PlacementSummary states have the correct cumulativeQuantity, cumulativePrice, and avgPrice
//        PlacementSummary alicePlacementSummary = alicePlacementSummaries.getStates().get(0).getState().getData();
//        assertEquals(quantity, alicePlacementSummary.getCumulativeQuantity());
//        assertEquals(price, alicePlacementSummary.getCumulativePrice());
//        assertEquals((float) price / quantity, alicePlacementSummary.getAvgPrice(), 0.0);
//
//        PlacementSummary bobPlacementSummary = bobPlacementSummaries.getStates().get(0).getState().getData();
//        assertEquals(quantity, bobPlacementSummary.getCumulativeQuantity());
//        assertEquals(price, bobPlacementSummary.getCumulativePrice());
//        assertEquals((float) price / quantity, bobPlacementSummary.getAvgPrice(), 0.0);
//    }
//}

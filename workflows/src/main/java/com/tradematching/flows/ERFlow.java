package com.tradematching.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.tradematching.contracts.ERContract;
import com.tradematching.contracts.PlacementSummaryContract;
import com.tradematching.states.ERState;
import com.tradematching.states.PlacementSummary;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.flows.FlowLogic;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ERFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class ERFlowInitiator extends FlowLogic<SignedTransaction>{

        //private variables
        private UUID placementId;
        private int quantity;
        private int price;

        //public constructor

        public ERFlowInitiator(UUID placementId, int quantity, int price) {
            this.placementId = placementId;
            this.quantity = quantity;
            this.price = price;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            Party broker = getOurIdentity();
            Party fidNode = getServiceHub().getNetworkMapCache().getPeerByLegalName(CordaX500Name.parse("O=Fidelity,L=New York,C=US"));

            List<StateAndRef<PlacementSummary>> placementSummaryStates = getServiceHub().getVaultService()
                    .queryBy(PlacementSummary.class)
                    .getStates()
                    .stream()
                    .filter(stateAndRef -> stateAndRef.getState().getData().getPlacementId().equals(placementId))
                    .collect(Collectors.toList());

            boolean placementSummaryExists = !placementSummaryStates.isEmpty();
            PlacementSummary currentPlacementSummary;

            if(placementSummaryExists) {
                currentPlacementSummary = placementSummaryStates.get(0).getState().getData();
            } else {
                currentPlacementSummary = new PlacementSummary(placementId, broker, fidNode, 0, 0, 0);
            }

            int newCumulativeQuantity = currentPlacementSummary.getCumulativeQuantity() + quantity;
            int newCumulativePrice = currentPlacementSummary.getCumulativePrice() + price;
            float newAveragePrice = (float) newCumulativePrice / newCumulativeQuantity;

            PlacementSummary updatePlacementSummary = new PlacementSummary(
                    currentPlacementSummary.getPlacementId(),
                    currentPlacementSummary.getBroker(),
                    currentPlacementSummary.getFidNode(),
                    newCumulativeQuantity,
                    newCumulativePrice,
                    newAveragePrice
            );

            ERState erState = new ERState(placementId, new UniqueIdentifier(), broker, fidNode, quantity, price);

            Command<ERContract.Commands.IssueER> erCommand = new Command<>(
                    new ERContract.Commands.IssueER(),
                    Arrays.asList(broker.getOwningKey(), fidNode.getOwningKey())
            );

            Command<PlacementSummaryContract.Commands.GenerateSummary> placementSummaryCommand =
                    new Command<>(new PlacementSummaryContract.Commands.GenerateSummary(),
                            Arrays.asList(broker.getOwningKey(), fidNode.getOwningKey()));

            TransactionBuilder txBuilder = new TransactionBuilder(getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0))
                    .addOutputState(updatePlacementSummary, PlacementSummaryContract.ID)
                    .addOutputState(erState, ERContract.ID)
                    .addCommand(erCommand)
                    .addCommand(placementSummaryCommand);

            if (placementSummaryExists) {
                txBuilder.addInputState(placementSummaryStates.get(0));
            }

            txBuilder.verify(getServiceHub());

            final SignedTransaction partiallySignedTx = getServiceHub().signInitialTransaction(txBuilder);

            List<FlowSession> sessions = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                    .map(node -> initiateFlow(node.getLegalIdentities().get(0)))
                    .collect(Collectors.toList());

            final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partiallySignedTx, sessions));

            return subFlow(new FinalityFlow(fullySignedTx, sessions));
        }
    }

    @InitiatedBy(ERFlowInitiator.class)
    public static class ERFlowResponder extends FlowLogic<Void>{
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public ERFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn't mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any aditional checks.
                     * */
                }
            });
            //Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}
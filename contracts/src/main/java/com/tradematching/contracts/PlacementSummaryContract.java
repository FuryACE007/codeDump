package com.tradematching.contracts;

import com.tradematching.states.ERState;
import com.tradematching.states.PlacementSummary;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class PlacementSummaryContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.tradematching.contracts.PlacementSummaryContract";

    // A transaction is valid if verify() method of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);

        if (command.getValue() instanceof Commands.GenerateSummary) {
            //Retrieve the output state of the transaction
            PlacementSummary output = tx.outputsOfType(PlacementSummary.class).get(0);

            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("Must be brokerA", output.getBroker().getName().equals(new CordaX500Name(
                        "BrokerA",
                        "London",
                        "GB"
                )));
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class GenerateSummary implements Commands {}
    }
}
package com.tradematching.contracts;

import com.tradematching.states.ERState;
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
public class ERContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.tradematching.contracts.ERContract";

    // A transaction is valid if verify() method of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        /* We can use the requireSingleCommand function to extract command data from transaction.
         * However, it is possible to have multiple commands in a single transaction.*/
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
//        final CommandData commandData = tx.getCommands().get(0).getValue();

        if (command.getValue() instanceof Commands.IssueER) {
            //Retrieve the output state of the transaction
            ERState output = tx.outputsOfType(ERState.class).get(0);

            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("Must be brokerA", output.getBroker().getName().equals(new CordaX500Name(
                        "BrokerA",
                        "London",
                        "GB"
                )));
                require.using("Quantity must be greater than 0", output.getQuantity() > 0);
                require.using("Price must be greater than 0", output.getPrice() > 0);
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class IssueER implements Commands {}
    }
}
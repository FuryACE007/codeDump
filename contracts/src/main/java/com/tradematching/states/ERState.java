package com.tradematching.states;

import com.tradematching.contracts.ERContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(ERContract.class)
public class ERState implements LinearState {

    //private variables
    private UUID placementId;
    private UniqueIdentifier erId;
    private Party broker;
    private Party fidNode;
    private int quantity;
    private int price;

    /* Constructor of your Corda state */

    public ERState(UUID placementId, UniqueIdentifier erId, Party broker, Party fidNode, int quantity, int price) {
        this.placementId = placementId;
        this.erId = erId;
        this.broker = broker;
        this.fidNode = fidNode;
        this.quantity = quantity;
        this.price = price;
    }

    //getters


    public UUID getPlacementId() {
        return placementId;
    }

    public UniqueIdentifier getErId() {
        return erId;
    }

    public Party getBroker() {
        return broker;
    }

    public Party getFidNode() {
        return fidNode;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(broker, fidNode);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.erId;
    }
}
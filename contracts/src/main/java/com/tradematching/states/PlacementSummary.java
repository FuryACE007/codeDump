package com.tradematching.states;

import com.tradematching.contracts.ERContract;
import com.tradematching.contracts.PlacementSummaryContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

// *********
// * State *
// *********
@BelongsToContract(PlacementSummaryContract.class)
public class PlacementSummary implements ContractState { // ContractState might be an issue

    //private variables
    private UUID placementId;
    private Party broker;
    private Party fidNode;
    private int cumulativeQuantity;
    private int cumulativePrice;
    private float avgPrice;

    /* Constructor of your Corda state */

    public PlacementSummary(UUID placementId, Party broker, Party fidNode, int cumulativeQuantity, int cumulativePrice, float avgPrice) {
        this.placementId = placementId;
        this.broker = broker;
        this.fidNode = fidNode;
        this.cumulativeQuantity = cumulativeQuantity;
        this.cumulativePrice = cumulativePrice;
        this.avgPrice = avgPrice;
    }
    //getters


    public UUID getPlacementId() {
        return placementId;
    }

    public Party getBroker() {
        return broker;
    }

    public Party getFidNode() {
        return fidNode;
    }

    public int getCumulativeQuantity() {
        return cumulativeQuantity;
    }

    public int getCumulativePrice() {
        return cumulativePrice;
    }

    public float getAvgPrice() {
        return avgPrice;
    }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(broker, fidNode);
    }
}
package uk.ac.standrews.cs.mamoc_client.DecisionMaker;

import uk.ac.standrews.cs.mamoc_client.Model.MamocNode;

public class NodeOffloadingPercentage {

    private MamocNode node;
    private double percentage;

    NodeOffloadingPercentage(MamocNode node, double percentage) {
        this.node = node;
        this.percentage = percentage;
    }

    public MamocNode getNode() {
        return node;
    }

    public double getPercentage() {
        return percentage;
    }
}

package com.evo.NEAT;

import java.util.ArrayList;

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 */
public class NodeGene {

    private float value;

    private ArrayList<ConnectionGene> incomingCon = new ArrayList<>();

    public NodeGene(float value) {
        super();
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public ArrayList<ConnectionGene> getIncomingCon() {
        return incomingCon;
    }

    public void setIncomingCon(ArrayList<ConnectionGene> incomingCon) {
        this.incomingCon = incomingCon;
    }

}

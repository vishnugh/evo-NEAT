package com.evo.NEAT;

/**
 * ConnectionGene Represents the connection(Axon) of the neuron
 * ConnectionGenes can completely represent the neuron as Nodes are generated while performing operation
 * Created by vishnughosh on 28/02/17.
 */
public class ConnectionGene {

    private int into,out, innovation;
    private float weight;
    private boolean enabled;

    public ConnectionGene(int into, int out, int innovation, float weight, boolean enabled) {
        this.into = into;
        this.out = out;
        this.innovation = innovation;
        this.weight = weight;
        this.enabled = enabled;
    }

    // Copy
    public ConnectionGene(ConnectionGene connectionGene){
        if(connectionGene!=null) {
            this.into = connectionGene.getInto();
            this.out = connectionGene.getOut();
            this.innovation = connectionGene.getInnovation();
            this.weight = connectionGene.getWeight();
            this.enabled = connectionGene.isEnabled();
        }
    }

    public int getInto() {
        return into;
    }

    public int getOut() {
        return out;
    }

    public int getInnovation() {
        return innovation;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    public String toString() {
/*        return "ConnectionGene{" +
                "into=" + into +
                ", out=" + out +
                ", innovation=" + innovation +
                ", weight=" + weight +
                ", enabled=" + enabled +
                '}';*/
        return into+","+out+","+weight+","+enabled;
    }
}

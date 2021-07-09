package com.evo.NEAT

/**
 * ConnectionGene Represents the connection(Axon) of the neuron
 * ConnectionGenes can completely represent the neuron as Nodes are generated while performing operation
 * Created by vishnughosh on 28/02/17.
 */
class ConnectionGene {
    var into = 0
        private set
    var out = 0
        private set
    var innovation = 0
        private set
    var weight = 0f
    var isEnabled = false

    constructor(into: Int, out: Int, innovation: Int, weight: Float, enabled: Boolean) {
        this.into = into
        this.out = out
        this.innovation = innovation
        this.weight = weight
        isEnabled = enabled
    }

    // Copy
    constructor(connectionGene: ConnectionGene?) {
        if (connectionGene != null) {
            into = connectionGene.into
            out = connectionGene.out
            innovation = connectionGene.innovation
            weight = connectionGene.weight
            isEnabled = connectionGene.isEnabled
        }
    }

    override fun toString(): String {
        return "ConnectionGene{" +
                "into=" + into +
                ", out=" + out +
                ", innovation=" + innovation +
                ", weight=" + weight +
                ", enabled=" + isEnabled +
                '}';
        return into.toString() + "," + out + "," + weight + "," + isEnabled
    }
}
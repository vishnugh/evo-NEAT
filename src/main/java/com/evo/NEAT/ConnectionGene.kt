package com.evo.NEAT

/**
 * ConnectionGene Represents the connection(Axon) of the neuron
 * ConnectionGenes can completely represent the neuron as Nodes are generated while performing operation
 * Created by vishnughosh on 28/02/17.
 *//*@Serializable*/
data class ConnectionGene(
    var keyInto: Int = 0,
    var keyOut: Int = 0,
    var innovationKey: Int = 0,
    var weight: Double = 0.0,
    var isEnabled: Boolean = false,
) {
    // Copy
    constructor(connectionGene: ConnectionGene) : this(
        keyInto = connectionGene.keyInto,
        keyOut = connectionGene.keyOut,
        innovationKey = connectionGene.innovationKey,
        weight = connectionGene.weight,
        isEnabled = connectionGene.isEnabled)
}

package com.evo.NEAT

import kotlinx.serialization.Serializable

/**
 * ConnectionGene Represents the connection(Axon) of the neuron
 * ConnectionGenes can completely represent the neuron as Nodes are generated while performing operation
 * Created by vishnughosh on 28/02/17.
 */@Serializable
data class ConnectionGene(
    var into: Int = 0,
    var out: Int = 0,
    var innovation: Int = 0,
    var weight: Double = 0.0,
    var isEnabled: Boolean = false,
) {
    // Copy
    constructor(connectionGene: ConnectionGene) : this(
        into = connectionGene.into,
        out = connectionGene.out,
        innovation = connectionGene.innovation,
        weight = connectionGene.weight,
        isEnabled = connectionGene.isEnabled)
}

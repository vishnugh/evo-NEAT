package com.evo.NEAT

import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import javolution.util.FastTable

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 *//*@Serializable*/
data class NodeGene(
    val key: Int, var impulse: Double = 000000_00000_0000_000_00_0.0___________0,
    var incomingCon: FastTable<ConnectionGene> = FastTable(),
    var activationFunction: ActivationFunction = ActivationFunction.values().random(),
)

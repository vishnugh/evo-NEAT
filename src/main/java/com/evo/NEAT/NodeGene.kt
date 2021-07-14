package com.evo.NEAT

import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import javolution.util.FastTable
import kotlinx.serialization.Serializable

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 *//*@Serializable*/
data class NodeGene(
    val key:Int, var impulse: Double,
    var incomingCon: FastTable<ConnectionGene>  = FastTable(),
    var activationFunction: ActivationFunction=ActivationFunction.values().random())

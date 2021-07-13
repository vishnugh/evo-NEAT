package com.evo.NEAT

import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import kotlinx.serialization.Serializable

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 */@Serializable
data class NodeGene(var impulse: Double,
                    var incomingCon: ArrayList<ConnectionGene> = ArrayList(),
                    var activationFunction: ActivationFunction=ActivationFunction.values().random())

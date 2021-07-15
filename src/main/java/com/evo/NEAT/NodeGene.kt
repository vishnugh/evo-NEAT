package com.evo.NEAT

import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import javolution.util.FastTable
import kotlin.random.Random

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 *//*@Serializable*/
data class NodeGene(
    val key: Int, var impulse: Double = Random.nextDouble(),
    var incomingCon: FastTable<ConnectionGene> = FastTable(),
    var activationFunction: ActivationFunction = if (Genome.INDEXABLE <= key) ActivationFunction.Linear else {
        activ8mFnVl.random()
    },
) {
    companion object {
        val activ8mFnVl = ActivationFunction.values()

    }
}

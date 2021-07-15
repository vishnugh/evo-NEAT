package com.evo.NEAT

import com.evo.NEAT.Genome.Companion.sim
import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import javolution.util.FastTable
import kotlin.random.Random

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 *//*@Serializable*/
open class NodeGene @JvmOverloads constructor(
    open val key: Int, open var impulse: Double = Random.nextDouble(),
    var incomingCon: FastTable<ConnectionGene> = FastTable(),
    open var activationFunction: ActivationFunction = if (    !((Genome.sim.INPUTS+1)until Genome.INDEXABLE ).contains(key) ) ActivationFunction.Linear else {
        activ8mFnVl.random()
    },
) {
    init{


    }
    constructor(parent: NodeGene) : this(
        parent.key,
        parent.impulse,
        parent.incomingCon.mapTo(FastTable<ConnectionGene>()) { ConnectionGene(it) },
        parent.activationFunction
    )
    companion object {
        val activ8mFnVl = ActivationFunction.values()
    }

    override fun toString(): String {
        return "NodeGene(key=$key, impulse=$impulse, incomingCon=$incomingCon, activationFunction=$activationFunction)"
    }
}

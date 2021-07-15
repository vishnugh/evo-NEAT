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
    open val key: Int,
    impulse1: Double = Random.nextDouble(),
    var incomingCon: FastTable<ConnectionGene> = if (key  <= sim.INPUTS) DUMMY else FastTable<ConnectionGene>(),
    activationFunction1: ActivationFunction = if (!((sim.INPUTS + 1) until Genome.INDEXABLE).contains(key)) ActivationFunction.Linear else {
        activ8mFnVl.random()
    },
) {


    open var impulse: Double = impulse1
        get() = if (key == sim.INPUTS) 1.0 else field


    open var activationFunction = activationFunction1

    constructor(parent: NodeGene) : this(
        parent.key,
        parent.impulse.takeUnless { parent.key == sim.INPUTS } ?: 1.0,
        parent.incomingCon.mapTo(FastTable<ConnectionGene>()) { ConnectionGene(it) },
        parent.activationFunction
    )

    companion object {
        val DUMMY by lazy {
            object : FastTable<ConnectionGene>() {init {
                unmodifiable()
            }

                override fun toString(): String {
                    return "DUMMYCOLLECTION"
                }
            }
        }
        val activ8mFnVl = ActivationFunction.values()
    }

    override fun toString(): String {
        return "NodeGene(key=$key, impulse=$impulse, incomingCon=$incomingCon, activationFunction=$activationFunction)"
    }
}

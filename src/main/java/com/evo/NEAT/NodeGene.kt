package com.evo.NEAT

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 */
class NodeGene(var value: Float) {
    var incomingCon = ArrayList<ConnectionGene>()
    override fun toString(): String {
        return "NodeGene(value=$value, incomingCon=$incomingCon)"
    }

}
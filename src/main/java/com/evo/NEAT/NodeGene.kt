package com.evo.NEAT

import kotlinx.serialization.Serializable

/**
 * NodeGene represents the nodes of the neural network
 * Created by vishnughosh on 28/02/17.
 */@Serializable
data class NodeGene(var value: Double, var incomingCon: ArrayList<ConnectionGene> = ArrayList())

package com.evo.NEAT.config

import kotlinx.serialization.Serializable

/*@Serializable*/
  open class Sim(
    val INPUTS: Int,/* = 2*/
    val STALE_POOL: Int, /*= 20*/
    val OUTPUTS: Int,/* = 1*/
    val HIDDEN_NODES: Int,/* = 1000000*/
    open val  POPULATION: Int, //= 300
    val SPECIES_DENOMINATOR: Int=6
)

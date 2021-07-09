package com.evo.NEAT

/**
 * assign Fitness to each genome
 * Created by vishnu on 12/1/17.
 *
 */
interface Environment {
    fun evaluateFitness(population: ArrayList<Genome>)
}
package com.evo.NEAT

import com.evo.NEAT.Genome.Companion.isSameSpecies
import com.evo.NEAT.Genome.Companion.rand
import com.evo.NEAT.Genome.Companion.sim
import com.evo.NEAT.config.NEAT_Config
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Created by vishnu on 7/1/17.
 */
@Serializable
class Pool {
    var species = ArrayList<Species>()
    private var generations = 0
    private val topFitness = 0.0
    private var poolStaleness = 0
    fun initializePool() {
        val nextInt = rand.nextInt(1, Genome.sim.POPULATION)
        for (i in 0 until nextInt) {
            addToSpecies(Genome())
        }
    }

    fun addToSpecies(g: Genome) =
        species.filter { it.genomes.isNotEmpty() && isSameSpecies(g, it.genomes.first()) }
            .firstOrNull { it.genomes.add(g) } ?: let { species.add(Species().also { it.genomes.add(g) }) }


    fun evaluateFitness(environment: Environment) {
        environment.evaluateFitness(species.map(Species::genomes).flatten())
        rankGlobally()
    }

    // experimental
    private fun rankGlobally() {                // set fitness to rank
        species.map(Species::genomes).flatten().sorted()
            .forEachIndexed { index: Int, value: Genome ->
                value.points = value.fitness
                value.fitness = index.toDouble()
            }


    }

    val topGenome get() = species.map(Species::genomes).flatten().sortedDescending().first()

    // all species must have the totalAdjustedFitness calculated
    fun calculateGlobalAdjustedFitness() = species.map(Species::totalAdjustedFitness).sum()

    fun removeWeakGenomesFromSpecies(allButOne: Boolean) {
        for (s in species) s.removeWeakGenomes(allButOne)
    }


    fun removeStaleSpecies() {
        val survived = ArrayList<Species>()
        if (topFitness < getTopFitness()) poolStaleness = 0
        for (s in species) {
            val top = s.topGenome
            if (top.fitness > s.topFitness) {
                s.topFitness = top.fitness
                s.staleness = 0
            } else {
                s.staleness = s.staleness + 1 // increment staleness
            }
            if (s.staleness < NEAT_Config.STALE_SPECIES || s.topFitness >= getTopFitness()) survived.add(s)
        }
        Collections.sort(survived, Collections.reverseOrder())
        if (poolStaleness > sim.STALE_POOL) for (i in survived.size downTo 2) survived.removeAt(i)
        species = survived
        poolStaleness++
    }

    fun calculateGenomeAdjustedFitness() {
        for (s in species) s.calculateGenomeAdjustedFitness()
    }

    fun breedNewGeneration(): ArrayList<Genome> {
        calculateGenomeAdjustedFitness()
        val survived = ArrayList<Species>()
        removeWeakGenomesFromSpecies(false)
        removeStaleSpecies()
        val globalAdjustedFitness = calculateGlobalAdjustedFitness()
        val children = ArrayList<Genome>()
        var carryOver = 0.0
        for (s in species) {
            val fchild = sim.POPULATION * (s.totalAdjustedFitness / globalAdjustedFitness)
            var nchild = fchild.toInt()
            carryOver += fchild - nchild
            if (carryOver > 1) {
                nchild++
                carryOver -= 1.0
            }
            if (1 <= nchild) {
                survived.add(Species(s.topGenome))
                for (i in 1 until nchild) {
                    val child = s.breedChild()
                    children.add(child)
                }
            }
        }
        species = survived
        for (child in children) addToSpecies(child)
        //clearInnovations();
        generations++
        return children
    }

    fun getTopFitness(): Double {
        var topFitness = 0.0
        var topGenome: Genome? = null
        for (s in species) {
            topGenome = s.topGenome
            if (topGenome.fitness > topFitness) {
                topFitness = topGenome.fitness
            }
        }
        return topFitness
    }

    val currentPopulation: Int
        get() {
            var p = 0
            for (s in species) p += s.genomes.size
            return p
        }
}

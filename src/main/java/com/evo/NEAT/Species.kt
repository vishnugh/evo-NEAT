package com.evo.NEAT

import com.evo.NEAT.Genome.Companion.crossOver
import com.evo.NEAT.config.NEAT_Config
import javolution.util.FastTable
import java.util.*
import kotlin.random.Random

/**
 * Created by vishnu on 7/1/17.
 */
/*@Serializable*/
class Species : Comparable<Species> {
    var genomes: MutableList<Genome> = mutableListOf()
    var topFitness = 0.0
        get() {
            field = topGenome.fitness
            return field
        }
    var staleness = 0

    constructor() : super()
    constructor(top: Genome) : super() {
        genomes.add(top)
    }

    fun calculateGenomeAdjustedFitness() = genomes.map { g ->
        (g.fitness / genomes.size).also { g.adjustedFitness = it }
    }.average()


    val totalAdjustedFitness: Double
        get() {
            var totalAdjustedFitness = 0.0
            for (g in genomes) {
                totalAdjustedFitness += g.adjustedFitness
            }
            return totalAdjustedFitness
        }

    private fun sortGenomes() {
        //sort internally genomes
        Collections.sort(genomes, Collections.reverseOrder())
    }

    fun removeWeakGenomes(allButOne: Boolean) {
/*
        sortGenomes()
        var surviveCount = 1
        if (!allButOne) surviveCount = Math.ceil((genomes.size / 2f).toDouble()).toInt()
        val survivedGenomes = FastTable<Genome>()
        for (i in 0 until surviveCount) survivedGenomes.add(Genome(genomes[i]))
        genomes = survivedGenomes
*/
    }

    @Deprecated("")
    fun removeWeakGenome(childrenToRemove: Int) {
/*
        sortGenomes()
        val survived = FastTable<Genome>()
        for (i in 0 until genomes.size - childrenToRemove) {
            survived.add(Genome(genomes[i]))
        }
        genomes = survived
*/
    }

    val topGenome: Genome
        get() {
            sortGenomes()
            return genomes.first()
        }

    fun breedChild(): Genome {
        var child: Genome
        child = if (Random.nextDouble() < NEAT_Config.CROSSOVER_CHANCE) {
            val g1 = genomes[Random.nextInt(genomes.size)]
            val g2 = genomes[Random.nextInt(genomes.size)]
            crossOver(g1, g2)
        } else {
            val g1 = genomes[Random.nextInt(genomes.size)]
            g1
        }
        child = Genome(child)
        child.Mutate()
        return child
    }

    override fun compareTo(o: Species): Int {
        val top = topFitness
        val otherTop = (o).topFitness
        return if (top == otherTop) 0 else if (top > otherTop) 1 else -1
    }
}

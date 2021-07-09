package com.evo.NEAT

import com.evo.NEAT.Genome.Companion.crossOver
import com.evo.NEAT.config.NEAT_Config
import java.util.*

/**
 * Created by vishnu on 7/1/17.
 */
class Species : Comparable<Any?> {
    var genomes = ArrayList<Genome>()
        private set
    var topFitness = 0f
        get() {
            field = topGenome.fitness
            field = topGenome.fitness
            return field
        }
    var staleness = 0
    var rand = Random()

    constructor() : super() {}
    constructor(top: Genome) : super() {
        genomes.add(top)
    }

    fun calculateGenomeAdjustedFitness() {
        for (g in genomes) {
            g.adjustedFitness = g.fitness / genomes.size
        }
    }

    val totalAdjustedFitness: Float
        get() {
            var totalAdjustedFitness = 0f
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
        sortGenomes()
        var surviveCount = 1
        if (!allButOne) surviveCount = Math.ceil((genomes.size / 2f).toDouble()).toInt()
        val survivedGenomes = ArrayList<Genome>()
        for (i in 0 until surviveCount) {
            survivedGenomes.add(Genome(genomes[i]))
        }
        genomes = survivedGenomes
    }

    @Deprecated("")
    fun removeWeakGenome(childrenToRemove: Int) {
        sortGenomes()
        val survived = ArrayList<Genome>()
        for (i in 0 until genomes.size - childrenToRemove) {
            survived.add(genomes[i])
        }
        genomes = survived
    }

    val topGenome: Genome
        get() {
            sortGenomes()
            return genomes[0]
        }

    fun breedChild(): Genome {
        var child: Genome
        child = if (rand.nextFloat() < NEAT_Config.CROSSOVER_CHANCE) {
            val g1 = genomes[rand.nextInt(genomes.size)]
            val g2 = genomes[rand.nextInt(genomes.size)]
            crossOver(g1, g2)
        } else {
            val g1 = genomes[rand.nextInt(genomes.size)]
            g1
        }
        child = Genome(child)
        child.Mutate()
        return child
    }

    override fun compareTo(o: Any?): Int {
        val s = o as Species?
        val top = topFitness
        val otherTop = s!!.topFitness
        return if (top == otherTop) 0 else if (top > otherTop) 1 else -1
    }
}
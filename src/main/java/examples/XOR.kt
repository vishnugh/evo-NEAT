package examples

import com.evo.NEAT.*
import com.evo.NEAT.MutationKeys.*
import com.evo.NEAT.config.Sim
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random

/**
 * Created by vishnughosh on 05/03/17.
 */
class XOR : Environment {
    @OptIn(ObsoleteCoroutinesApi::class)
    override fun evaluateFitness(population: Iterable<Genome>) {
        runBlocking {
            for (gene in population)
                launch {
                    gene.fitness = DoubleArray(max(1,fidelity/*Random.nextInt((fidelity*.90).toInt(), fidelity)*/)) {
                        val (i, j) = (Random.nextBoolean()) to (Random.nextBoolean())
                        val inputs = doubleArrayOf(if (i) 1.0 else 0.0, if (j) 1.0 else 0.0)
                        val output = gene.evaluateNetwork(inputs)
                        val expected = if (i xor j) 1.0 else 0.0
                        (1.0 - kotlin.math.abs(expected - output[0]))
                    }.average()
                }
        }
    }


    companion object {
        var fidelity = 1

        var generation = 0
        val xor = XOR()

        @JvmStatic
        fun main(arg0: Array<String>) {
            Genome.sim = object : Sim(2, 20, 1, 1000000, 100, 3) {
                override val POPULATION: Int get() = max(generation * 2, (INPUTS + OUTPUTS + 2)*5)

            }

            val pool = Pool()
            pool.initializePool()
            var topGenome: Genome

            var ladder = 0.1

            while (true) {
                //pool.evaluateFitness();
                pool.evaluateFitness(xor)
                topGenome = pool.topGenome
                println("TopFitness : ${topGenome.points} vs ladder: $ladder by width $fidelity")

                if (topGenome.points > ladder) {
                    ladder = topGenome.points
                    if (ladder == 1.0) {
                        ladder = .1
                        fidelity++
                    }
                    println("nodes access  hit(${accesses.get() to (accesses.get() - misses.get())})/miss(${misses.get()}) ratio ${topGenome.run { accesses.toDouble() / misses.toDouble() }}")
                    println("GenomeAdjustedFitness: ${pool.calculateGenomeAdjustedFitness()}")
                    println("species : " + pool.species.size)
                    println(topGenome.toString())
                    var special: Species? = null
                    pool.species.withIndex().forEach { (i, species) ->
                        print("species #$i size:${species.genomes.size}")
                        if (topGenome in species.genomes) {
                            special = species
                            print(" *** ")
                        }
                        println()
                    }


                    topGenome.mutationRates[PERTURB_CHANCE] = 0.0
                    topGenome.mutationRates[WEIGHT_CHANCE] = 0.0
                    topGenome.mutationRates[WEIGHT_MUTATION_CHANCE] = 0.0
                    topGenome.mutationRates[NODE_MUTATION_CHANCE] = 0.0
                    topGenome.mutationRates[CONNECTION_MUTATION_CHANCE] = 0.0
                    topGenome.mutationRates[BIAS_CONNECTION_MUTATION_CHANCE] = 0.0
                    topGenome.mutationRates[DISABLE_MUTATION_CHANCE] = 0.0
                    topGenome.mutationRates[ENABLE_MUTATION_CHANCE] = 0.0

                    //single-survivor with no variance is a bad idea.

//                    pool.species = mutableListOf(Species().also { it.genomes += (topGenome) })

                }

                    pool.species.apply {
                        if (size > 5) dropLast(size / 2)
                        forEach {
                            it.genomes.apply {
                                if (size > 5) dropLast(size / 2)
                            }
                    }
                }

                println("Generation : ${generation to pool.currentPopulation to "Species: ${pool.species.size}"} ")
                pool.breedNewGeneration()

                generation++
            }

        }

    }

}


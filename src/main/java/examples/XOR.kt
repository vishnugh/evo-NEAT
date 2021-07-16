package examples

import com.evo.NEAT.*
import com.evo.NEAT.MutationKeys.*
import com.evo.NEAT.config.Sim
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.sqrt
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
                    gene.fitness = DoubleArray(max(1, fidelity)) {
                        BooleanArray(2) { (Random.nextBoolean()) }.let { inn ->
                            val inputs = DoubleArray(2) { if (inn[it]) 1.0 else 0.0 }
                            val output = gene.evaluateNetwork(inputs)
                            val expected = if (inn[0].xor(inn[1])) 1.0 else 0.0
                            1.0 - kotlin.math.abs(expected - output[0])
                        }
                    }.average()
                }
        }
    }

    companion object {
        var fidelity = 1

        var generation = 1
        val xor = XOR()

        @JvmStatic
        fun main(arg0: Array<String>) {
            Genome.sim = object : Sim(2, 20, 1, 1000000, 100, 3) {
                override val POPULATION: Int get() = max(generation * 2, (INPUTS + OUTPUTS + 2) * 5)

            }

            var champions: MutableList<Genome> = mutableListOf()
            val pool = Pool()
            pool.initializePool()
            var topGenome: Genome

            var ladder = 0.1

            while (true) {
                //pool.evaluateFitness();
                pool.evaluateFitness(xor)
                if (generation > 2) {
                    topGenome = pool.topGenome
                    val fidelitySqrt = sqrt(fidelity.toDouble())
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

                        if (champions.size > 10) {
                            val mutableList =
                                champions
                                    .sortedByDescending { it.adjustedFitness }
                                    .dropLast(champions.size - 10)
                                    .toMutableList()
                            champions = mutableList
                        }
                        champions.add(Genome(topGenome)
                            .also {
                                it.adjustedFitness =
                                    sqrt(generation.toDouble()) + it.points * fidelitySqrt / max((1.0).toDouble(),
                                        it.connections.filter { it.isEnabled }.size.toDouble())
                            })
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
                }
                pool.species.apply {
                    if (size > 5) pool.species = dropLast(size / 2).toMutableList()
                    forEach {
                        it.genomes.also { mutableList ->
                            if (mutableList.size > 5) it.genomes =
                                mutableList.dropLast(mutableList.size / 2).toMutableList()
                        }
                    }
                    if (Random.nextBoolean() && champions.isNotEmpty()) {
                        val parent = champions.random()
                        val g = Genome(parent)
                        pool.addToSpecies(g)
                    }
                }

                println("Generation : ${generation to pool.currentPopulation to "Species: ${pool.species.size}"} ")
                pool.breedNewGeneration()

                generation++
            }

        }

    }

}


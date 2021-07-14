package examples

import com.evo.NEAT.*
import com.evo.NEAT.config.Sim
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Math.abs
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
                    gene.fitness = DoubleArray(17) {
                        val (i, j) = (Random.nextBoolean()) to (Random.nextBoolean())
                        val inputs = doubleArrayOf(if (i) 1.0 else 0.0, if (j) 1.0 else 0.0)
                        val output = gene.evaluateNetwork(inputs)
                        val expected = if (i xor j) 1.0 else 0.0
                        (1.0 - kotlin.math.abs(expected - output[0]))
                    }.average().toDouble()
                }
        }
    }

    companion object {

        val xor = XOR()

        @JvmStatic
        fun main(arg0: Array<String>) {
            Genome.sim = Sim(2, 20, 1, 1000000, 1500, 3)
            val pool = Pool()
            pool.initializePool()
            var topGenome: Genome


            var generation = 0
            while (true) {
                //pool.evaluateFitness();
                pool.evaluateFitness(xor)
                topGenome = pool.topGenome
                println("TopFitness : " + topGenome.points)

                if (topGenome.points > 0.94) {
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


                    pool.species = mutableListOf(special!!)

                }

                println("Generation : ${generation to pool.currentPopulation to "Species: ${pool.species.size}"} ")
                pool.breedNewGeneration()

                generation++
            }

        }

    }

}


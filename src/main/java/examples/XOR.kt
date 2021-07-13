package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool
import com.evo.NEAT.Species
import com.evo.NEAT.config.Sim
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Math.abs
import kotlin.random.Random

/**
 * Created by vishnughosh on 05/03/17.
 */
class XOR : Environment {
    @OptIn(ObsoleteCoroutinesApi::class)
    override fun evaluateFitness(population: Iterable<Genome>) {
        runBlocking(context) {
            for (gene in population) launch(context) {
                gene.fitness = DoubleArray(17) {
                    val (i, j) = (Random.nextBoolean()) to (Random.nextBoolean())
                    val inputs = doubleArrayOf(if (i) 1.0 else 0.0, if (j) 1.0 else 0.0)
                    val output = gene.evaluateNetwork(inputs)
                    val expected = if (i xor j) 1.0 else 0.0
                    (1.0 - abs(expected - output[0]))
                }.average().toDouble()
            }
        }
    }

    companion object {

        val xor = XOR()
        val context = Genome.context

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

                    println("GenomeAdjustedFitness: ${pool.calculateGenomeAdjustedFitness()}")
                    println("species : " + pool.species.size)
                    println(topGenome.toString())
                    var special: Species? = null
                    for ((i, species) in pool.species.withIndex()) {
                        print("species #$i size:${species.genomes.size}")
                        if (topGenome in species.genomes) {
                            special = species
                            print(" *** ")
                        }
                        println()
                    }


//                    special!!.genomes
                    pool.species = mutableListOf(special!!)

//                    println(Json {
//                        isLenient = true; allowSpecialFloatingPointValues = true;prettyPrint = false
//                    }.encodeToString(pool))

                }

                println("Generation : ${generation to pool.currentPopulation to "Species: ${pool.species.size}"} ")
                pool.breedNewGeneration()

                generation++
            }

        }

    }

}


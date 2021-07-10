package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Math.abs
import kotlin.random.Random

/**
 * Created by vishnughosh on 05/03/17.
 */
class SineRider : Environment {
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

        const val INPUTS = 1
        const val STALE_POOL = 20
        const val OUTPUTS = 3

        /** apparently this is a numeric key offset that seperates the genome state
         *  from input and output nodes in a stable offset key */
        const val HIDDEN_NODES = 1000000

        val sineRider = SineRider()
        val context = Genome.context

        @JvmStatic
        fun main(arg0: Array<String>) {
            val pool = Pool()
            pool.initializePool()
            var topGenome: Genome
            var generation = 0
            while (true) {
                //pool.evaluateFitness();
                pool.evaluateFitness(sineRider)
                topGenome = pool.topGenome
                println("TopFitness : " + topGenome.points)

                if (topGenome.points > 0.95f) {
                    break
                }

                println("Generation : $generation")
                pool.breedNewGeneration()
                generation++
            }

            System.err.println("GenomeAdjustedFitness: ${pool.calculateGenomeAdjustedFitness()}")
            println(topGenome.evaluateNetwork(doubleArrayOf(1.0, 0.0))[0])
            System.out.println("Population : " + pool.currentPopulation)
        }
    }
}
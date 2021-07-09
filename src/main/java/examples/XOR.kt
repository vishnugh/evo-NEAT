package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
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

                gene.fitness = FloatArray(17) {
                    val (i, j) = (Random.nextBoolean()) to (Random.nextBoolean())

                    val inputs = floatArrayOf(if (i) 1f else 0f, if (j) 1f else 0f)
                    val output = gene.evaluateNetwork(inputs)
                    val expected = if (i xor j) 1f else 0f

                    (1f - abs(expected - output[0]))
                }.average().toFloat()
            }
        }
    }


    companion object {
        val xor = XOR()
val context=Genome.context
        @JvmStatic
        fun main(arg0: Array<String>) {
            val pool = Pool()
            pool.initializePool()
            var topGenome: Genome
            var generation = 0
            while (true) {
                //pool.evaluateFitness();
                pool.evaluateFitness(xor)
                topGenome = pool.topGenome
                println("TopFitness : " + topGenome.points)

                if (topGenome.points > 0.95f) {
                    break
                }
                //                System.out.println("Population : " + pool.currentPopulation)

                println("Generation : $generation")
                //           System.out.println("Total number of matches played : "+TicTacToe.matches);
                pool.breedNewGeneration()
                generation++
            }

            System.err.println("GenomeAdjustedFitness: ${pool.calculateGenomeAdjustedFitness()}");
            println(topGenome.evaluateNetwork(floatArrayOf(1f, 0f))[0])
            println ("$topGenome")
        }
    }
}
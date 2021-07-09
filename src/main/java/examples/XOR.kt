package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.lang.Math.abs
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Created by vishnughosh on 05/03/17.
 */
class XOR : Environment {
    @OptIn(ObsoleteCoroutinesApi::class)
    override fun evaluateFitness(population: ArrayList<Genome>) {
        runBlocking(newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(),"population"), ){

                for (gene in population) launch {

                gene.fitness = FloatArray(17) {
                    val (i, j) = (Random.nextBoolean()) to (Random.nextBoolean())

                    val inputs = floatArrayOf(if (i) 1f else 0f, if (j) 1f else 0f)
                    val output = gene.evaluateNetwork(inputs)
                    val expected = if (i xor j) 1f else 0f

//                System.err.println(i to j to expected)

                    (1f - abs(expected - output[0])).toFloat()
                }.average().toFloat()
            }
        }
    }

    companion object {
        val xor = XOR()

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
                if (topGenome.points > 0.95) break
                //                System.out.println("Population : " + pool.currentPopulation)
                println("Generation : $generation")
                //           System.out.println("Total number of matches played : "+TicTacToe.matches);
                //           pool.calculateGenomeAdjustedFitness();
                pool.breedNewGeneration()
                generation++
            }
            println(topGenome.evaluateNetwork(floatArrayOf(1f, 0f))[0])
        }
    }
}
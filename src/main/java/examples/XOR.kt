package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool
import java.lang.Math.abs
import kotlin.math.pow

/**
 * Created by vishnughosh on 05/03/17.
 */
class XOR : Environment {
    override fun evaluateFitness(population: ArrayList<Genome>) {
        for (gene in population) {
            var fitness = 0f
            gene.fitness = 0f
            var r = listOf<Float>()
            for (i in 0..1)
                for (j in 0..1) {
                    val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                    val output = gene.evaluateNetwork(inputs)
                    val expected = i xor j
//                    System.out.println("Inputs are ${i to j} output ${output[0]} Answer : ${i xor j}")

                    val fl = 1f - abs(expected.toFloat() - output[0])
                    r += fl
                }
            gene.fitness = r.average().toFloat()
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
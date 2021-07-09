package examples

import com.evo.NEAT.Environment
import com.evo.NEAT.Genome
import com.evo.NEAT.Pool

/**
 * Created by vishnughosh on 05/03/17.
 */
class XOR : Environment {
    override fun evaluateFitness(population: ArrayList<Genome>?) {
        for (gene in population!!) {
            var fitness = 0f
            gene.fitness = 0f
            for (i in 0..1) for (j in 0..1) {
                val inputs = floatArrayOf(i.toFloat(), j.toFloat())
                val output = gene.evaluateNetwork(inputs)
                val expected = i xor j
                //                  System.out.println("Inputs are " + inputs[0] +" " + inputs[1] + " output " + output[0] + " Answer : " + (i ^ j));
                //if (output[0] == (i ^ j))
                fitness += 1 - Math.abs(expected - output[0])
            }
            fitness = fitness * fitness
            gene.fitness = fitness
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
                if (topGenome.points > 15) {
                    break
                }
                //            System.out.println("Population : " + pool.getCurrentPopulation() );
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
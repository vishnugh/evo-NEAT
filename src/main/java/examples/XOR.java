package examples;

import com.evo.NEAT.Environment;
import com.evo.NEAT.Genome;
import com.evo.NEAT.Pool;

import java.util.ArrayList;

/**
 * Created by vishnughosh on 05/03/17.
 */
public class XOR implements Environment {
    @Override
    public void evaluateFitness(ArrayList<Genome> population) {

        for (Genome gene: population) {
            float fitness = 0;
            gene.setFitness(0);
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 2; j++) {
                    float inputs[] = {i, j};
                    float output[] = gene.evaluateNetwork(inputs);
                    int expected = i^j;
                    //                  System.out.println("Inputs are " + inputs[0] +" " + inputs[1] + " output " + output[0] + " Answer : " + (i ^ j));
                    //if (output[0] == (i ^ j))
                    fitness +=  (1 - Math.abs(expected - output[0]));
                }
            fitness = fitness * fitness;

            gene.setFitness(fitness);

        }

    }

    public static void main(String arg0[]){
        XOR xor = new XOR();

        Pool pool = new Pool();
        pool.initializePool();

        Genome topGenome = new Genome();
        int generation = 0;
        while(true){
            //pool.evaluateFitness();
            pool.evaluateFitness(xor);
            topGenome = pool.getTopGenome();
            System.out.println("TopFitness : " + topGenome.getPoints());

            if(topGenome.getPoints()>15){
                break;
            }
//            System.out.println("Population : " + pool.getCurrentPopulation() );
            System.out.println("Generation : " + generation );
            //           System.out.println("Total number of matches played : "+TicTacToe.matches);
            //           pool.calculateGenomeAdjustedFitness();

            pool.breedNewGeneration();
            generation++;

        }
        System.out.println(topGenome.evaluateNetwork(new float[]{1,0})[0]);
    }
}

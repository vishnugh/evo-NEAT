package com.evo.NEAT;

import java.util.ArrayList;

/**
 * assign Fitness to each genome
 * Created by vishnu on 12/1/17.
 *
 */
public interface Environment {

     void evaluateFitness(ArrayList<Genome> population);

}

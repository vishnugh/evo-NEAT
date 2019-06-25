# evo-NEAT
A java implementation of NEAT(NeuroEvolution of Augmenting Topologies ) for the generation of evolving artificial neural networks.

Implimentation of http://nn.cs.utexas.edu/downloads/papers/stanley.ec02.pdf

### Usage
All projects should have a class which implements Environment interface and override its evaluateFitness method. The evaluateFitness method is the method to assign the fitness value to the genomes depending on the problem(Environment). 

The main function should have a Pool and the Environment instance. 

Every generation should call the pool.evaluateFitness(env) and pool.breedNewGeneration().
The best fitness score of the pool can be accessed by calling pool.getTopGenome().getPoints(). This can be used as a break condition as well. 

An example of the XOR implementation is given in the folder evo-NEAT/src/examples/  .

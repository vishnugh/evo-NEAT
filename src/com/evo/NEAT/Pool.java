package com.evo.NEAT;

/**
 * Created by vishnu on 7/1/17.
 */

import com.evo.NEAT.com.evo.NEAT.config.NEAT_Config;

import java.util.ArrayList;
import java.util.Collections;

public class Pool {


    private ArrayList<Species> species = new ArrayList<>();
    private int generations = 0;
    private float topFitness ;
    private int poolStaleness = 0;


    public ArrayList<Species> getSpecies() {
        return species;
    }

    public void initializePool() {

        for (int i = 0; i < NEAT_Config.POPULATION; i++) {
            addToSpecies(new Genome());
        }

    }

    public void addToSpecies(Genome g) {
        for (Species s : species) {
            if (s.getGenomes().size() == 0)
                continue;
            Genome g0 = s.getGenomes().get(0);
//		System.out.println(s.genomes.size());
            if (Genome.isSameSpecies(g, g0)) {
                s.getGenomes().add(g);
                return;
            }
        }
        Species childSpecies = new Species();
        childSpecies.getGenomes().add(g);
        species.add(childSpecies);
    }

    public void evaluateFitness() {         //For Testing
        for (Species s : species)
            for (Genome g : s.getGenomes()) {
                float fitness = 0;
                g.setFitness(0);
                for (int i = 0; i < 2; i++)
                    for (int j = 0; j < 2; j++) {
                        float inputs[] = {i, j};
                        float output[] = g.evaluateNetwork(inputs);
                        int expected = i^j;
      //                  System.out.println("Inputs are " + inputs[0] +" " + inputs[1] + " output " + output[0] + " Answer : " + (i ^ j));
                        //if (output[0] == (i ^ j))
                            fitness +=  (1 - Math.abs(expected - output[0]));
                    }
                    fitness = fitness * fitness;// * fitness * fitness;

                if(fitness>15)
                    System.out.println("Fitness : " + fitness);
                g.setFitness(fitness);
                //System.out.println("Fitness : "+fitness);
            }
            rankGlobally();
    }

    public void evaluateFitness(Environment environment){

        ArrayList<Genome> allGenome = new ArrayList<>();

        for(Species s: species){
            for(Genome g: s.getGenomes()){
                allGenome.add(g);
            }
        }

 /*       for(int i =0; i<allGenome.size(); i++){
            for(int j = 0; j<allGenome.size(); j++){
                if(i!=j){
                    Genome player1 = allGenome.get(i);
                    Genome player2 = allGenome.get(j);
                    environment.match(player1,player2);
                }
            }
        }*/

        environment.evaluateFitness(allGenome);
        rankGlobally();
    }
    // experimental
    private void rankGlobally(){                // set fitness to rank
        ArrayList<Genome> allGenome = new ArrayList<>();

        for(Species s: species){
            for(Genome g: s.getGenomes()){
                allGenome.add(g);
            }
        }
        Collections.sort(allGenome);
  //      allGenome.get(allGenome.size()-1).writeTofile();
 //       System.out.println("TopFitness : "+ allGenome.get(allGenome.size()-1).getFitness());
        for (int i =0 ; i<allGenome.size(); i++) {
            allGenome.get(i).setPoints(allGenome.get(i).getFitness());      //TODO use adjustedFitness and remove points
            allGenome.get(i).setFitness(i);
        }
    }

    public Genome getTopGenome(){
        ArrayList<Genome> allGenome = new ArrayList<>();

        for(Species s: species){
            for(Genome g: s.getGenomes()){
                allGenome.add(g);
            }
        }
        Collections.sort(allGenome,Collections.reverseOrder());

        return allGenome.get(0);
    }
    // all species must have the totalAdjustedFitness calculated
    public float calculateGlobalAdjustedFitness() {
        float total = 0;
        for (Species s : species) {
            total += s.getTotalAdjustedFitness();
        }
        return total;
    }

    public void removeWeakGenomesFromSpecies(boolean allButOne){
        for(Species s: species){
            s.removeWeakGenomes(allButOne);
        }
    }

    public void removeStaleSpecies(){
        ArrayList<Species> survived = new ArrayList<>();

        if(topFitness<getTopFitness()){
            poolStaleness = 0;
        }

        for(Species s: species){
            Genome top  = s.getTopGenome();
            if(top.getFitness()>s.getTopFitness()){
                s.setTopFitness(top.getFitness());
                s.setStaleness(0);
            }
            else{
                s.setStaleness(s.getStaleness()+1);     // increment staleness
            }

            if(s.getStaleness()< NEAT_Config.STALE_SPECIES || s.getTopFitness()>= this.getTopFitness()){
                survived.add(s);
            }
        }

        Collections.sort(survived,Collections.reverseOrder());

        if(poolStaleness>NEAT_Config.STALE_POOL){
            for(int i = survived.size(); i>1 ;i--)
            survived.remove(i);
        }

        species = survived;
        poolStaleness++;
    }

    public void calculateGenomeAdjustedFitness(){
        for (Species s: species) {
            s.calculateGenomeAdjustedFitness();
        }
    }
    public ArrayList<Genome> breedNewGeneration() {


        calculateGenomeAdjustedFitness();
        ArrayList<Species> survived = new ArrayList<>();

        removeWeakGenomesFromSpecies(false);
        removeStaleSpecies();
        float globalAdjustedFitness = calculateGlobalAdjustedFitness();
        ArrayList<Genome> children = new ArrayList<>();
        float carryOver = 0;
        for (Species s : species) {
            float fchild = NEAT_Config.POPULATION * (s.getTotalAdjustedFitness() / globalAdjustedFitness) ;//- 1;       // reconsider
            int nchild = (int) fchild;
            carryOver += fchild - nchild;
            if (carryOver > 1) {
                nchild++;
                carryOver -= 1;
            }

            if(nchild < 1)
                continue;

            survived.add(new Species(s.getTopGenome()));
            //s.removeWeakGenome(nchild);

            //children.add(s.getTopGenome());
            for (int i = 1; i < nchild; i++) {
                Genome child = s.breedChild();
                children.add(child);
            }


        }
        species = survived;
        for (Genome child: children)
            addToSpecies(child);
        //clearInnovations();
        generations++;
        return children;
    }

    public float getTopFitness(){
        float topFitness = 0;
        Genome topGenome =null;
        for(Species s : species){
            topGenome = s.getTopGenome();
            if(topGenome.getFitness()>topFitness){
                topFitness = topGenome.getFitness();
            }
        }
        return topFitness;
    }

    public int getCurrentPopulation() {
        int p = 0;
        for (Species s : species)
            p += s.getGenomes().size();
        return p;
    }


}

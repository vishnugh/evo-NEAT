package com.evo.NEAT

import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import com.evo.NEAT.config.NEAT_Config
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import javax.management.RuntimeErrorException
import kotlin.random.Random

/**
 * Created by vishnughosh on 28/02/17.
 */
class Genome : Comparable<Genome> {
    constructor() {
        mutationRates = MutationKeys.mutationRates
    }

    constructor(child: Genome) {
        this.connectionGeneList = ArrayList<ConnectionGene>()
        this.nodes = TreeMap<Int, NodeGene>()
        this.mutationRates = MutationKeys.mutationRates
        for (c in child.connectionGeneList) {
            connectionGeneList.add(ConnectionGene(c))
        }
        fitness = child.fitness
        adjustedFitness = child.adjustedFitness
        color=child.color
        mutationRates = EnumMap(child.mutationRates)
    }

    var color = listOf(
        ActivationFunction.SigmoidActivationFunction,
//        ActivationFunction.RectifierActivationFunction,
        ActivationFunction.TanhActivationFunction,
//        ActivationFunction.CosineActivationFunction,
//        ActivationFunction.NegatedLinearActivationFunction,
//        ActivationFunction.SqrtActivationFunction
    ).shuffled()
        .first()
    var fitness // Global Percentile Rank (higher the better)
            = 0f
    var points = 0f

    // Can remove below setter-getter after testing
    var connectionGeneList: MutableList<ConnectionGene> = ArrayList() // DNA- MAin archive of gene information
    var nodes: SortedMap<Int, NodeGene> = TreeMap() // Generated while performing network operation
    var adjustedFitness // For number of child to breed in species
            = 0f
    private var mutationRates = EnumMap(MutationKeys.mutationRates)

    enum class MutationKeys(val cfg: Number, val fn: ((Genome) -> Unit) = {/* no-op */ }) {
        STEPS(NEAT_Config.STEPS),
        PERTURB_CHANCE(NEAT_Config.PERTURB_CHANCE),
        WEIGHT_CHANCE(NEAT_Config.WEIGHT_CHANCE),
        WEIGHT_MUTATION_CHANCE(NEAT_Config.WEIGHT_MUTATION_CHANCE, Genome::mutateWeight),
        NODE_MUTATION_CHANCE(NEAT_Config.NODE_MUTATION_CHANCE, { it.mutateAddConnection(false) }),
        CONNECTION_MUTATION_CHANCE(NEAT_Config.CONNECTION_MUTATION_CHANCE, { it.mutateAddConnection(true) }),
        BIAS_CONNECTION_MUTATION_CHANCE(NEAT_Config.BIAS_CONNECTION_MUTATION_CHANCE, Genome::mutateAddNode),
        DISABLE_MUTATION_CHANCE(NEAT_Config.DISABLE_MUTATION_CHANCE, Genome::disableMutate),
        ENABLE_MUTATION_CHANCE(NEAT_Config.ENABLE_MUTATION_CHANCE, Genome::enableMutate),
        ;

        companion object {
            val mutationRates
                get() = EnumMap<MutationKeys, Number>(MutationKeys::class.java).apply {
                    values().forEach { mutationKeys: MutationKeys -> put(mutationKeys, mutationKeys.cfg) }
                }
        }
    }


    private fun generateNetwork() {
        nodes.clear()
        //  Input layer
        for (i in 0 until NEAT_Config.INPUTS) {
            nodes[i] = NodeGene(0f) //Inputs
        }
        nodes[NEAT_Config.INPUTS] = NodeGene(1f) // Bias

        //output layer
        for (i in NEAT_Config.INPUTS + NEAT_Config.HIDDEN_NODES until NEAT_Config.INPUTS + NEAT_Config.HIDDEN_NODES + NEAT_Config.OUTPUTS) {
            nodes[i] = NodeGene(0.0f)
        }

        // hidden layer
        for (con in connectionGeneList) {
            if (!nodes.containsKey(con.into)) nodes[con.into] = NodeGene(0f)
            if (!nodes.containsKey(con.out)) nodes[con.out] = NodeGene(0f)
            nodes[con.out]!!.incomingCon.add(con)
        }
    }

    fun evaluateNetwork(inputs: FloatArray): FloatArray {
        val output = FloatArray(NEAT_Config.OUTPUTS)
        generateNetwork()
        for (i in 0 until NEAT_Config.INPUTS) {
            nodes[i]!!.value = inputs[i]
        }
        for ((key, node) in nodes) {
            var sum = 0f
            if (key > NEAT_Config.INPUTS) {
                for (conn in node.incomingCon)
                    if (conn.isEnabled) sum += nodes[conn.into]!!.value * conn.weight
                node.value = doColor(sum)
            }
        }
        for (i in 0 until NEAT_Config.OUTPUTS) {
            output[i] = nodes[NEAT_Config.INPUTS + NEAT_Config.HIDDEN_NODES + i]!!.value
        }
        return output
    }

    private fun doColor(x: Float): Float {
        return color.apply(x.toDouble()).toFloat()
    }

    // Mutations
    fun Mutate() {
        // Mutate mutation rates
        for ((key, value) in mutationRates) {
            if (rand.nextBoolean()) mutationRates[key] = 0.95f * value.toFloat() else mutationRates[key] =
                1.05263f * value.toFloat()
        }
        (MutationKeys.WEIGHT_MUTATION_CHANCE.ordinal..MutationKeys.ENABLE_MUTATION_CHANCE.ordinal).forEach {
            MutationKeys.values()[it].let { mk ->
                if (this.mutationRates[mk]!!.toFloat() > rand.nextFloat()) {
                    mk.fn(this)
                }
            }
        }
    }

    fun mutateWeight() {
        for (c in connectionGeneList) {
            if (rand.nextFloat() < NEAT_Config.WEIGHT_CHANCE) {
                if (rand.nextFloat() < NEAT_Config.PERTURB_CHANCE) c.weight =
                    c.weight + (2 * rand.nextFloat() - 1) * NEAT_Config.STEPS else c.weight = 4 * rand.nextFloat() - 2
            }
        }
    }

    fun mutateAddConnection(forceBais: Boolean) {
        generateNetwork()
        var i = 0
        var j = 0
        val random2 = rand.nextInt(nodes.size - NEAT_Config.INPUTS - 1) + NEAT_Config.INPUTS + 1
        var random1 = rand.nextInt(nodes.size)
        if (forceBais) random1 = NEAT_Config.INPUTS
        var node1 = -1
        var node2 = -1
        for (k in nodes.keys) {
            if (random1 == i) {
                node1 = k
                break
            }
            i++
        }
        for (k in nodes.keys) {
            if (random2 == j) {
                node2 = k
                break
            }
            j++
        }
        //	System.out.println("random1 = "+random1 +" random2 = "+random2);
//	System.out.println("Node1 = "+node1 +" node 2 = "+node2);
        if (node1 >= node2) return
        for (con in nodes[node2]!!.incomingCon) {
            if (con.into == node1) return
        }
        if (node1 < 0 || node2 < 0) throw RuntimeErrorException(null) // TODO Pool.newInnovation(node1, node2)
        connectionGeneList.add(ConnectionGene(node1,
            node2,
            InnovationCounter.newInnovation(),
            4 * rand.nextFloat() - 2,
            true)) // Add innovation and weight
    }

    fun mutateAddNode() {
        generateNetwork()
        if (connectionGeneList.size > 0) {
            var timeoutCount = 0
            var randomCon = connectionGeneList[rand.nextInt(connectionGeneList.size)]
            while (!randomCon.isEnabled) {
                randomCon = connectionGeneList[rand.nextInt(connectionGeneList.size)]
                timeoutCount++
                if (timeoutCount > NEAT_Config.HIDDEN_NODES) return
            }
            val nextNode = nodes.size - NEAT_Config.OUTPUTS
            randomCon.isEnabled = false
            connectionGeneList.add(ConnectionGene(randomCon.into,
                nextNode,
                InnovationCounter.newInnovation(),
                1f,
                true)) // Add innovation and weight
            connectionGeneList.add(ConnectionGene(nextNode,
                randomCon.out,
                InnovationCounter.newInnovation(),
                randomCon.weight,
                true))
        }
    }

    fun disableMutate() {
        //generateNetwork();                // remove laters
        if (connectionGeneList.size > 0) {
            val randomCon = connectionGeneList[rand.nextInt(connectionGeneList.size)]
            randomCon.isEnabled = false
        }
    }

    fun enableMutate() {
//        generateNetwork();                // remove laters
        if (connectionGeneList.size > 0) {
            val randomCon = connectionGeneList[rand.nextInt(connectionGeneList.size)]
            randomCon.isEnabled = true
        }
    }

    override fun compareTo(other: Genome): Int = fitness.compareTo(other.fitness)

    override fun toString(): String {
        return "Genome{" +
                "fitness=" + fitness +
                ", connectionGeneList=" + connectionGeneList +
                ", nodeGenes=" + nodes +
                '}'
    }

    fun writeTofile() {
        var bw: BufferedWriter? = null
        var fw: FileWriter? = null
        val builder = StringBuilder()
        for (conn in connectionGeneList) {
            builder.append("""
    $conn
    
    """.trimIndent())
        }
        try {
            fw = FileWriter("Genome.txt")
            bw = BufferedWriter(fw)
            bw.write(builder.toString())
            println("Done")
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bw?.close()
                fw?.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    companion object {
        private val rand = Random

        @JvmStatic
        fun crossOver(parent1: Genome, parent2: Genome): Genome {
            var parent1 = parent1
            var parent2 = parent2
            if (parent1.fitness < parent2.fitness) {
                val temp = parent1
                parent1 = parent2
                parent2 = temp
            }
            val child = Genome()
            val geneMap1 = TreeMap<Int, ConnectionGene?>()
            val geneMap2 = TreeMap<Int, ConnectionGene?>()
            for (con in parent1.connectionGeneList) {
                assert(!geneMap1.containsKey(con.innovation) //TODO Remove for better performance
                )
                geneMap1[con.innovation] = con
            }
            for (con in parent2.connectionGeneList) {
                assert(!geneMap2.containsKey(con.innovation) //TODO Remove for better performance
                )
                geneMap2[con.innovation] = con
            }
            val innovationP1: Set<Int> = geneMap1.keys
            val innovationP2: Set<Int> = geneMap2.keys
            val allInnovations: MutableSet<Int> = LinkedHashSet(innovationP1)
            allInnovations.addAll(innovationP2)
            for (key in allInnovations) {
                var trait: ConnectionGene?
                if (geneMap1.containsKey(key) && geneMap2.containsKey(key)) {
                    trait = if (rand.nextBoolean()) {
                        ConnectionGene(geneMap1[key])
                    } else {
                        ConnectionGene(geneMap2[key])
                    }
                    if (geneMap1[key]!!.isEnabled != geneMap2[key]!!.isEnabled) {
                        trait.isEnabled = rand.nextFloat() >= 0.75f
                    }
                } else if (parent1.fitness == parent2.fitness) {               // disjoint or excess and equal fitness
                    trait = if (geneMap1.containsKey(key)) geneMap1[key] else geneMap2[key]
                    if (rand.nextBoolean()) {
                        continue
                    }
                } else trait = geneMap1[key]
                trait?.let { child.connectionGeneList.add(it) }
            }
            return child
        }

        @JvmStatic
        fun isSameSpecies(g1: Genome, g2: Genome): Boolean {


            val geneMap1 = g1.connectionGeneList.map { connectionGene -> connectionGene.innovation to connectionGene }
                .toMap(sortedMapOf())//.toSortedMap()
            val geneMap2 = g2.connectionGeneList.map { connectionGene -> connectionGene.innovation to connectionGene }
                .toMap(sortedMapOf())//.toSortedMap()

            val lowMaxInnovation: Int = when {
                geneMap1.isEmpty() || geneMap2.isEmpty() -> 0
                else -> Math.min(geneMap1.lastKey(), geneMap2.lastKey())
            }

            val keys1 = geneMap1.keys
            val keys2 = geneMap2.keys

            (keys1 + keys2).toMutableSet().let { remainder ->
                var matching = 0
                var disjoint = 0
                var excess = 0
                var weight = 0f
                keys1.intersect(keys2).forEach {
                    remainder -= it
                    matching++
                    weight += Math.abs(geneMap1[it]!!.weight - geneMap2[it]!!.weight)
                }
                remainder.forEach {
                    when {
                        it < lowMaxInnovation -> disjoint++
                        else -> excess++
                    }
                }
                //System.out.println("matching : "+matching + "\ndisjoint : "+ disjoint + "\nExcess : "+ excess +"\nWeight : "+ weight);
                return ((matching + disjoint + excess).takeIf { N -> N < 0 }?.let { N ->
                    (NEAT_Config.EXCESS_COEFFICENT * excess + NEAT_Config.DISJOINT_COEFFICENT * disjoint) / N + NEAT_Config.WEIGHT_COEFFICENT * weight / matching
                } ?: 0f).let { delta ->
                    delta < NEAT_Config.COMPATIBILITY_THRESHOLD
                }

            }
        }
    }
}


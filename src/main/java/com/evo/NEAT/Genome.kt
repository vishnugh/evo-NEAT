package com.evo.NEAT

import com.evo.NEAT.config.NEAT_Config
import com.evo.NEAT.config.Sim
import examples.SineRider.Companion.HIDDEN_NODES
import examples.SineRider.Companion.INPUTS
import examples.SineRider.Companion.OUTPUTS
import javolution.util.FastTable
import kotlinx.coroutines.newFixedThreadPoolContext
//import org.eclipse.collections.api.tuple.primitive.IntObjectPair
//import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import javax.management.RuntimeErrorException
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Created by vishnughosh on 28/02/17.
 */
/*@Serializable*/
class Genome : Comparable<Genome> {
    constructor() {
        mutationRates = MutationKeys.mutationRates
    }

    constructor(child: Genome) {
        this.connections = FastTable<ConnectionGene>()
        this.nodes = sortedMapOf()
        this.mutationRates = MutationKeys.mutationRates
        for (c in child.connections) connections.add(ConnectionGene(c))
        fitness = child.fitness
        adjustedFitness = child.adjustedFitness
        mutationRates = EnumMap(child.mutationRates)
//        color = child.color
    }

    /* var color: ActivationFunction = ActivationFunction.values().random()*/

    /** Global Percentile Rank (higher the better)*/
    var fitness = 0.0
    var points = 0.0

    // Can remove below setter-getter after testing
    var connections: MutableList<ConnectionGene> = FastTable() // DNA- MAin archive of gene information

    /*@Serializable*/
    var nodes: MutableMap<Int, NodeGene> = sortedMapOf()

    // Generated while performing network operation
    var adjustedFitness = 0.0// For number of child to breed in species

    /*@Serializable*/
    private var mutationRates: MutableMap<MutationKeys, Double> = EnumMap(MutationKeys.mutationRates)

    fun generateNetwork() {
        /* Bias output layer */
        nodes = ((List(sim.INPUTS) { i -> i to NodeGene(0.0) } +
                (INPUTS + HIDDEN_NODES until INPUTS + HIDDEN_NODES + OUTPUTS).map {
                    it to NodeGene(0.0)
                }) +
                (INPUTS to NodeGene(1.0) /* Bias output layer */)).toMap(linkedMapOf())
        // hidden layer

        connections.forEach { con ->
            if (!nodes.containsKey(con.into)) nodes[con.into] = NodeGene(0.0)
            if (!nodes.containsKey(con.out)) nodes[con.out] =
                NodeGene(0.0, FastTable<ConnectionGene>().also { it += con })
        }


    }

    fun evaluateNetwork(inputs: DoubleArray): DoubleArray {
        val output = DoubleArray(OUTPUTS)
        generateNetwork()
        for (i in 0 until INPUTS) nodes[i]!!.impulse = inputs[i]
        nodes.filter { it.key > INPUTS }.forEach { (key, node) ->
            node.incomingCon.filter {
                it.isEnabled
            }.map { (into, out, innovation, weight, isEnabled): ConnectionGene ->
                nodes[into]!!.impulse * weight
            }.sum().let { sum -> node.impulse = node.activationFunction(sum) }

        }
        val i1 = INPUTS + HIDDEN_NODES
        for (i in 0 until OUTPUTS) output[i] = run {
            nodes[i1 + i]!!.activationFunction.invoke(nodes[i1 + i]!!.impulse)
        }
        return output
    }

    /*   private fun doColor(x: Double): Double {
           return color.apply(x.toDouble()).toDouble()
       }*/

    // Mutations
    fun Mutate() {
        // Mutate mutation rates
        for ((key, value) in mutationRates) {
            if (rand.nextBoolean()) mutationRates[key] = 0.95 * value.toDouble() else mutationRates[key] =
                1.05263f * value.toDouble()
        }
        (MutationKeys.WEIGHT_MUTATION_CHANCE.ordinal..MutationKeys.ENABLE_MUTATION_CHANCE.ordinal).forEach {
            MutationKeys.values()[it].let { mk ->
                if (this.mutationRates[mk]!!.toDouble() > rand.nextDouble()) mk.fn(this)
            }
        }
    }

    fun mutateWeight() {
        for (c in connections) if (rand.nextDouble() < NEAT_Config.WEIGHT_CHANCE)
            if (rand.nextDouble() < NEAT_Config.PERTURB_CHANCE) c.weight =
                c.weight + (rand.nextDouble(-1.0, (1.0 + Double.MIN_VALUE))) * NEAT_Config.STEPS else c.weight =
                rand.nextDouble(-2.0, 2.0).toDouble()
    }

    fun mutateAddConnection(forceBais: Boolean) {
        generateNetwork()
        var i = 0
        var j = 0
        val random2 = rand.nextInt(INPUTS + 1, nodes.size)
        var random1 = rand.nextInt(nodes.size)
        if (forceBais) random1 = INPUTS
        var node1 = -1
        var node2 = -1

        for (k in nodes.keys) {
            if (random1 == i) {
                k.also { node1 = it }
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
        connections.add(
            ConnectionGene(
                node1,
                node2,
                InnovationCounter.newInnovation(),
                4 * rand.nextDouble() - 2,
                true
            )
        ) // Add innovation and weight
    }

    fun mutateAddNode() {
        generateNetwork()
        if (connections.size > 0) {
            var timeoutCount = 0
            var randomCon = connections[rand.nextInt(connections.size)]
            while (!randomCon.isEnabled) {
                randomCon = connections[rand.nextInt(connections.size)]
                timeoutCount++
                if (timeoutCount > HIDDEN_NODES) return
            }
            val nextNode = nodes.size - OUTPUTS
            randomCon.isEnabled = false
            connections.add(
                ConnectionGene(
                    randomCon.into,
                    nextNode,
                    /**
                     * identifying string
                     */
                    InnovationCounter.newInnovation(),
                    1.0,
                    true
                )
            ) // Add innovation and weight
            connections.add(
                ConnectionGene(
                    nextNode,
                    randomCon.out,
                    InnovationCounter.newInnovation(),
                    randomCon.weight,
                    true
                )
            )
        }
    }

    fun disableMutate() {
        //generateNetwork();                // remove laters
        if (connections.size > 0) {
            val randomCon = connections[rand.nextInt(connections.size)]
            randomCon.isEnabled = false
        }
    }

    fun enableMutate() {
//        generateNetwork();                // remove laters
        if (connections.size > 0) {
            val randomCon = connections[rand.nextInt(connections.size)]
            randomCon.isEnabled = true
        }
    }

    override fun compareTo(other: Genome): Int = fitness.compareTo(other.fitness)


    fun writeTofile() {
        var bw: BufferedWriter? = null
        var fw: FileWriter? = null
        val builder = StringBuilder()
        for (conn in connections) {
            builder.append(
                """
    $conn
    
    """.trimIndent()
            )
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
        val rand = Random

        @JvmStatic
        fun crossOver(p1: Genome, p2: Genome): Genome {
            var parent1 = p1
            var parent2 = p2

            if (parent1.fitness < parent2.fitness) {
                val temp = parent1
                parent1 = parent2
                parent2 = temp
            }
            val child = Genome()
            val geneMap1 = TreeMap<Int, ConnectionGene>()
            val geneMap2 = TreeMap<Int, ConnectionGene>()
            for (con in parent1.connections) geneMap1[con.innovation] = con
            for (con in parent2.connections) geneMap2[con.innovation] = con
            val innovationP1: Set<Int> = geneMap1.keys
            val innovationP2: Set<Int> = geneMap2.keys
            val allInnovations: MutableSet<Int> = LinkedHashSet(innovationP1)
            allInnovations.addAll(innovationP2)
            for (key in allInnovations) {
                var trait: ConnectionGene?
                if (geneMap1.containsKey(key) && geneMap2.containsKey(key)) {
                    trait = ConnectionGene((if (rand.nextBoolean()) geneMap1 else geneMap2)[key]!!)
                    if (geneMap1[key]!!.isEnabled != geneMap2[key]!!.isEnabled) trait.isEnabled =
                        rand.nextDouble() >= 0.75f
                } else if (parent1.fitness != parent2.fitness) trait = geneMap1[key]
                else {               // disjoint or excess and equal fitness
                    trait = if (geneMap1.containsKey(key)) geneMap1[key] else geneMap2[key]
                    if (rand.nextBoolean()) continue
                }
                trait?.let { child.connections.add(it) }
            }
            return child
        }

        @JvmStatic
        fun isSameSpecies(g1: Genome, g2: Genome): Boolean {
            val sz1 = g1.connections.size
            val sz2 = g2.connections.size
            val combinedSize = sz1 + sz2

            val geneMap1 = g1.connections.iterator()
            val geneMap2 = g2.connections.iterator()

            val matchSize = combinedSize / max(1, sim.SPECIES_DENOMINATOR)
            val rollingSize = min(sz1, sz2)
            var tally = 0
            if (rollingSize >= matchSize) {
                val mette = LinkedHashSet<Int>(combinedSize - matchSize)
                val intRange = 0 until max(sz1, sz2)
                for (x: Int in intRange) {
                    if (geneMap1.hasNext() && !mette.add(geneMap1.next().innovation)) tally++
                    if (geneMap2.hasNext() && !mette.add(geneMap2.next().innovation)) tally++
                    if (tally >= matchSize) return true
                }
            }
            return false
        }

        val context = newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "population")

        lateinit var sim: Sim

    }

    override fun toString(): String {
        val unqLinks = connections.filter { it.isEnabled }
        val inList: List<Int> = unqLinks.map { it.into }.distinct()
        val outList: List<Int> = unqLinks.map { it.out }.distinct()
        val allinks: List<Int> = inList + outList
        return "Genome(fitness=$fitness, points=$points, adjustedFitness=$adjustedFitness," +
                "mutationRates=$mutationRates, connectionGeneList={" +
                "size=${unqLinks.size}, in=$inList, out=$outList }}}, nodes={size=${nodes.size}," +
                "nodes=${
                    nodes.filter {
                        it.key in allinks
                    }.map {
                        it.value.impulse to it.value.incomingCon.size to it.value.activationFunction
                    }
                }})"
    }
}

//operator fun <V> IntObjectHashMap<V>.set(i: Int, value: V) = this.put(i, value)
//operator fun <T> IntObjectPair<T>.component1() = one
//operator fun <T> IntObjectPair<T>.component2() = two

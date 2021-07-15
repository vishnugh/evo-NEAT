@file:Suppress("NOTHING_TO_INLINE")

package com.evo.NEAT

import com.evo.NEAT.Genome.Companion.INDEXABLE
import com.evo.NEAT.Genome.Companion.sim
import com.evo.NEAT.com.evo.NEAT.ActivationFunction
import com.evo.NEAT.config.NEAT_Config
import com.evo.NEAT.config.Sim
import javolution.util.FastSet
import javolution.util.FastSortedMap
import javolution.util.FastSortedTable
import javolution.util.FastTable
import javolution.util.function.Equality
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.management.RuntimeErrorException
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Created by vishnughosh on 28/02/17.
 */
/*@Serializable*/
class Genome(
    /*@Serializable*/
    val nodes: FastSortedTable<NodeGene> = FastSortedTable<NodeGene>(object : Equality<NodeGene> {
        override fun compare(left: NodeGene, right: NodeGene): Int = left.key.compareTo(right.key)
        override fun hashCodeOf(it: NodeGene) = it.key
        override fun areEqual(left: NodeGene?, right: NodeGene?) = left?.key == right?.key
    }),
    /*@Serializable*/
    val mutationRates: MutableMap<MutationKeys, Double> = EnumMap(MutationKeys.mutationRates),
    /** Global Percentile Rank (higher the better)*/
    var fitness: Double = 0.0,
    var points: Double = 0.0,
    val connections: FastTable<ConnectionGene> = FastTable(),
    var adjustedFitness: Double = 0.0,
) : Comparable<Genome> {

//    mutationRates = MutationKeys.mutationRates

    constructor(parent: Genome) : this() {
        parent.connections.mapTo(connections) { ConnectionGene(it) }
        parent.nodes.mapTo(this.nodes) {
            NodeGene(it.takeUnless { (it.key == sim.INPUTS) }?: INPUTBIAS)
        }
        fitness = parent.fitness
        adjustedFitness = parent.adjustedFitness
        mutationRates.putAll(parent.mutationRates)
        points = parent.points
    }


    /* var color: ActivationFunction = ActivationFunction.values().random()*/

    fun generateNetwork() {//TODO: dirty flag

        if (nodes.isEmpty()) {
            nodes
                .apply { for (i in 0 until sim.INPUTS) add(NodeGene(i)) }
                .apply { add(INPUTBIAS) }
                .apply {
                    for (i in INDEXABLE until INDEXABLE + sim.OUTPUTS) add(NodeGene(i).also {
                        it.activationFunction = ActivationFunction.Linear
                    } /* extra supervision  */)
                }
        }

        // hidden layer
        connections.forEach { con ->
            if (!nodes.any { it.key == con.keyInto }) nodes.addLast(NodeGene(con.keyInto))
            if (!nodes.any { it.key == con.keyOut }) nodes.addLast(
                NodeGene(con.keyOut, con.weight, FastTable<ConnectionGene>().also { it += ConnectionGene(con) }))
        }
        assert(true)
    }


    fun evaluateNetwork(inputs: DoubleArray): DoubleArray {
        generateNetwork()
        for (i in 0 until sim.INPUTS) (+nodes)(i).impulse = inputs[i]
        nodes.filtered {
            it.key >= sim.INPUTS
        }.forEach { node ->
            val toDownStream = node.incomingCon.filter {
                it.isEnabled
            }.map { (inKey, _, _, weight, _): ConnectionGene ->
                val upstreamNode = (+nodes)(inKey)
                val impulse = upstreamNode.impulse
                val res = impulse * weight
                res
            }
            var final: Double
            toDownStream.sum().let { sum ->
                node.run {
                    final = activationFunction(sum)
                    impulse = final
                }
            }
            assert(true)
        }

        return DoubleArray(sim.OUTPUTS) { i ->
            (-nodes)(INDEXABLE + i).impulse
        }
    }

/*   private fun doColor(x: Double): Double {
       return color.apply(x.toDouble()).toDouble()
   }*/

    // Mutations
    fun Mutate() {
        // Mutate mutation rates
        for ((key, value) in mutationRates)
            if (rand.nextBoolean()) mutationRates[key] = 0.95 * value else mutationRates[key] =
                1.05263f * value
        for (it in MutationKeys.WEIGHT_MUTATION_CHANCE.ordinal..MutationKeys.ENABLE_MUTATION_CHANCE.ordinal) {
            MutationKeys.values()[it].let { mk ->
                if (this.mutationRates[mk]!!.toDouble() > rand.nextDouble()) mk.fn(this)
            }
        }
    }

    fun mutateWeight() {
        for (c in connections) if (rand.nextDouble() < NEAT_Config.WEIGHT_CHANCE)
            if (rand.nextDouble() < NEAT_Config.PERTURB_CHANCE) c.weight =
                c.weight + (rand.nextDouble(-1.0, (1.0 + Double.MIN_VALUE))) * NEAT_Config.STEPS else c.weight =
                rand.nextDouble(-2.0, 2.0)
    }


    /**
     * tmfm happens here.
     */
    fun mutateAddConnection(forceBais: Boolean) {
        generateNetwork()
        var i = 0
        var j = 0
        val random2 = rand.nextInt(nodes.size - sim.INPUTS - 1) + sim.INPUTS + 1
        var random1 = rand.nextInt(nodes.size)
        if (forceBais) random1 = sim.INPUTS
        var node1 = -1
        var node2 = -1
        for (k in nodes.mapped(NodeGene::key)) {
            if (random1 == i) {
                node1 = k
                break
            }
            i++
        }
        for (k in nodes.mapped(NodeGene::key)) {
            if (random2 == j) {
                node2 = k
                break
            }
            j++
        }
        //	System.out.println("random1 = "+random1 +" random2 = "+random2);
//	System.out.println("Node1 = "+node1 +" node 2 = "+node2);
        if (node1 >= node2) return
        for (con in (-nodes)(node2)!!.incomingCon) {
            if (con.keyInto == node1) return
        }
        if (node1 < 0 || node2 < 0) throw RuntimeErrorException(null) // TODO Pool.newInnovation(node1, node2)
        connections.add(ConnectionGene(node1,
            node2,
            innovation.getAndIncrement(),
            rand.nextDouble(),
            true)) // Add innovation and weight
    }

    fun mutateAddNode() {
        generateNetwork()
        if (connections.size > 0) {
            var timeoutCount = 0
            var randomCon = connections[rand.nextInt(connections.size)]
            while (!randomCon.isEnabled) {
                randomCon = connections[rand.nextInt(connections.size)]
                timeoutCount++
                if (timeoutCount > sim.HIDDEN_NODES) return
            }
            val nextNode = nodes.size - sim.OUTPUTS
            randomCon.isEnabled = false
            connections.add(
                ConnectionGene(
                    randomCon.keyInto,
                    nextNode,
                    /**
                     * identifying string
                     */
                    innovation.getAndIncrement(),
                    1.0,
                    true
                )
            ) // Add innovation and weight
            connections.add(
                ConnectionGene(
                    nextNode,
                    randomCon.keyOut,
                    innovation.getAndIncrement(),
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
        val innovation = AtomicInteger(0)

        val INPUTBIAS by lazy {
            object : NodeGene(sim.INPUTS, 1.0, FastTable(), ActivationFunction.Linear) {
                override val key get() = Genome.sim.INPUTS
                override var impulse
                    get() = 1.0
                    set(value) {}
                override var activationFunction
                    get() = ActivationFunction.Linear
                    set(value) {}
            }
        }

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
            val geneMap1 = FastSortedMap<Int, ConnectionGene>()
            val geneMap2 = FastSortedMap<Int, ConnectionGene>()
            for (con in parent1.connections) geneMap1[con.innovationKey] = con
            for (con in parent2.connections) geneMap2[con.innovationKey] = con
            val innovationP1: FastSet<Int> = geneMap1.keys
            val innovationP2: FastSet<Int> = geneMap2.keys
            val allInnovations = FastSet<Int>()
            allInnovations.addAll(innovationP1)
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
                val mette = FastSet<Int>()//combinedSize - matchSize
                val intRange = 0 until max(sz1, sz2)
                for (x: Int in intRange) {
                    if (geneMap1.hasNext() && !mette.add(geneMap1.next().innovationKey)) tally++
                    if (geneMap2.hasNext() && !mette.add(geneMap2.next().innovationKey)) tally++
                    if (tally >= matchSize) return true
                }
            }
            return false
        }

        lateinit var sim: Sim
        val INDEXABLE: Int by lazy { (sim).run { INPUTS + HIDDEN_NODES } }

    }

    override fun toString(): String {
        val unqLinks = connections.filter { it.isEnabled }
        val inList: List<Int> = unqLinks.map { it.keyInto }.distinct()
        val outList: List<Int> = unqLinks.map { it.keyOut }.distinct()
        val allinks: List<Int> = inList + outList
        return "Genome(fitness=$fitness, points=$points, adjustedFitness=$adjustedFitness," +
                "mutationRates=$mutationRates, connectionGeneList={" +
                "size=${unqLinks.size}, in=$inList, out=$outList }}}, nodes={size=${nodes.size}," +
                "nodes=${
                    nodes.filtered {
                        it.key in allinks
                    }.map {
                        it.impulse to it.incomingCon.size to it.activationFunction
                    }
                }})"
    }

}

inline operator fun FastSortedTable<NodeGene>.unaryMinus() = { x: Int -> this.filtered { x == it.key }.first() }
val misses = AtomicInteger(0)
val accesses = AtomicInteger(0)
inline operator fun FastSortedTable<NodeGene>.unaryPlus() = { x: Int ->
    accesses.getAndIncrement().let {
        if (x > INDEXABLE) (-this)(x) else
            this[x].takeIf { sim.INPUTS >= x || x == it.key }
                ?: (-this)(x).also { misses.getAndIncrement() }
    }
}


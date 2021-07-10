package com.evo.NEAT

import com.evo.NEAT.config.NEAT_Config
import kotlinx.serialization.Serializable
import java.util.*
@Serializable
enum class MutationKeys(val cfg: Double, val fn: ((Genome) -> Unit) = {/* no-op */ }) {
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
            get() = EnumMap<MutationKeys, Double>(MutationKeys::class.java).apply {
                values().forEach { mutationKeys: MutationKeys -> put(mutationKeys, mutationKeys.cfg.toDouble()) }
            }
    }
}
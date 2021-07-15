package com.evo.NEAT

import com.evo.NEAT.config.NEAT_Config
import java.util.*
/*@Serializable*/
enum class MutationKeys(val cfg: Double, val fn: ((Genome) -> Unit) = {/* no-op */ }) {
    STEPS(NEAT_Config.STEPS),
    PERTURB_CHANCE(NEAT_Config.PERTURB_CHANCE),
    WEIGHT_CHANCE(NEAT_Config.WEIGHT_CHANCE),
    WEIGHT_MUTATION_CHANCE(0.9, Genome::mutateWeight),
    NODE_MUTATION_CHANCE(0.1, { it.mutateAddConnection(false) }),
    CONNECTION_MUTATION_CHANCE(0.1, { it.mutateAddConnection(true) }),
    BIAS_CONNECTION_MUTATION_CHANCE(0.15, Genome::mutateAddNode),
    DISABLE_MUTATION_CHANCE(0.1, Genome::disableMutate),
    ENABLE_MUTATION_CHANCE(0.2, Genome::enableMutate),
    ;

    companion object {
        val mutationRates
            get() = EnumMap<MutationKeys, Double>(MutationKeys::class.java).apply {
                values().forEach { mutationKeys: MutationKeys -> put(mutationKeys, mutationKeys.cfg.toDouble()) }
            }
    }
}

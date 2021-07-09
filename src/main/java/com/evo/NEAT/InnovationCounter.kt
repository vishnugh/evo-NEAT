package com.evo.NEAT

/**
 * Created by vishnughosh on 04/03/17.
 */
object InnovationCounter {
    private var innovation = 0
    fun newInnovation(): Int {
        innovation++
        return innovation
    }
}
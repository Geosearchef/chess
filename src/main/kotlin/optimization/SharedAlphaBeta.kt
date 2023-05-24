package optimization

import kotlin.math.max
import kotlin.math.min

class SharedAlphaBeta(var alpha: Double = -100000.0, var beta: Double = +100000.0) {

    fun update(alpha: Double = this.alpha, beta: Double = this.beta) {
        synchronized(this) {
            this.alpha = max(this.alpha, alpha)
            this.beta = min(this.beta, beta)
        }
    }

}
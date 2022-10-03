package optimization

import Board

class TranspositionTable(val table: MutableMap<Long, Transposition> = HashMap()) {

    var stores = 0L
    var lookups = 0L
    var hits = 0L

    // does not check if already present!
    fun put(board: Board, depth: Int, score: Double) = put(board.hash, depth, score)
    fun put(hash: Long, depth: Int, score: Double) {
        table[hash + depth] = Transposition(hash, depth, score)
        stores++
    }

    fun lookup(board: Board, depth: Int) = lookup(board.hash, depth)
    fun lookup(hash: Long, depth: Int): Transposition? {
        val entry = table[hash + depth]

        lookups++

        if(entry == null || entry.depth != depth) {
            return null
        } else {
            hits++
//            return null
            return entry
        }
    }

    override fun toString() = "TranspositionTable:  entries: ${table.entries.size}, stores: ${stores}, lookups: $lookups, hits: $hits, hit rate: ${
        kotlin.math.round(hits.toDouble() / lookups.toDouble() * 10000) / 100
    } %"

    fun clone() = TranspositionTable(HashMap(table))

    companion object {
        fun union(vararg table: TranspositionTable): TranspositionTable {
            val newTable = TranspositionTable()

            table.forEach { newTable.table.putAll(it.table) }

            return newTable
        }
    }
}

class Transposition(val hash: Long, val depth: Int, val score: Double) {

}
import kotlin.math.sign


class Coords(val x: Int, val y: Int) {
    val representation: String get() = "${('a' + x)}${y + 1}"

    constructor(r: String) : this(r[0] - 'a', r[1].digitToInt() - 1)

    operator fun plus(other: Coords): Coords = Coords(this.x + other.x, this.y + other.y)
    operator fun minus(other: Coords): Coords = Coords(this.x - other.x, this.y - other.y)

    override fun toString() = representation
    fun isInBounds(board: Board) = x >= 0 && y >= 0 && x < board.size && y < board.size

    fun sign() = Coords(this.x.sign, this.y.sign)

    override fun equals(other: Any?): Boolean {
        return (other as? Coords)?.let { it.x == this.x && it.y == this.y } ?: false
    }
}

fun String.toCoords() = Coords(this)


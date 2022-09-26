import Player.BLACK
import Player.WHITE
import java.util.*
import kotlin.collections.ArrayList


class Board(init: Boolean = true, val size: Int = 8) {

    val pieces: Array<Array<Int>> = Array(8) { Array(8) { NONE } }

    val squares: ArrayList<Coords> = ArrayList<Coords>().apply {
        for(x in indicesX()) {
            for(y in indicesY()) {
                add(Coords(x, y))
            }
        }
    }

    constructor(src: Board) : this() {
        squares.forEach {
            pieces[it.x][it.y] = src.pieces[it.x][it.y]
        }
    }

    init {
        if(init) {
            initializeBoard()
        }
    }

    fun initializeBoard() {
        for(x in indicesX()) {
            pieces[x][1] = PAWN_WHITE
            pieces[x][6] = PAWN_BLACK
        }
        pieces[0][0] = ROOK_WHITE
        pieces[7][0] = ROOK_WHITE
        pieces[0][7] = ROOK_BLACK
        pieces[7][7] = ROOK_BLACK

        pieces[1][0] = KNIGHT_WHITE
        pieces[6][0] = KNIGHT_WHITE
        pieces[1][7] = KNIGHT_BLACK
        pieces[6][7] = KNIGHT_BLACK

        pieces[2][0] = BISHOP_WHITE
        pieces[5][0] = BISHOP_WHITE
        pieces[2][7] = BISHOP_BLACK
        pieces[5][7] = BISHOP_BLACK

        pieces[4][0] = QUEEN_WHITE
        pieces[3][0] = KING_WHITE
        pieces[4][7] = QUEEN_BLACK
        pieces[3][7] = KING_BLACK
    }


    override fun toString(): String {
        val sb = StringBuilder()
        for(y in indicesY()) {
            if(y != 0) {
                sb.append("\n")
            }
            for(x in indicesX()) {
                if(x != 0) {
                    sb.append("  ")
                }
                sb.append(pieces[x][y].getPieceRepresentation())
            }
        }

        return sb.toString()
    }

    fun println() {
        println(this.toString())
    }


    fun getPossibleMoves(player: Player) = getPossibleMovesForBoard(this, player)

    fun piece(coords: Coords) = pieces[coords.x][coords.y]
    fun empty(coords: Coords) = piece(coords) == NONE

    fun indicesX() = 0 until size
    fun indicesY() = 0 until size
}

fun main(args: Array<String>) {
    val board = Board()
    board.println()

    val possibleMoves = board.getPossibleMoves(WHITE)
    println("\nPossible moves: ${possibleMoves.size}")
    possibleMoves.forEach { println(it) }
}
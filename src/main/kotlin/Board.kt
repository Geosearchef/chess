import Player.WHITE
import engine.Evaluator
import optimization.ZobristHashing
import optimization.ZobristTable
import kotlin.collections.ArrayList


class Board(init: Boolean = true, val size: Int = 8) {

    val pieces: Array<Array<Int>> = Array(8) { Array(8) { NONE } }
    var lastMove: Move? = null

    var hash: Long = 0L

    var squares: ArrayList<Coords> = ArrayList()

    // optimization
    var whiteKingTaken = false
        private set
    var blackKingTaken = false
        private set
    val kingTaken get() = whiteKingTaken || blackKingTaken

    constructor(src: Board) : this() {
        squares = src.squares

        squares.forEach {
            pieces[it.x][it.y] = src.pieces[it.x][it.y]
        }

        lastMove = src.lastMove
    }

    init {
        if(squares.isEmpty()) {
            squares = ArrayList<Coords>().apply {
                for(x in indicesX()) {
                    for(y in indicesY()) {
                        add(Coords(x, y))
                    }
                }
            }
        }

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

        initializeHash()
    }

    fun initializeHash() {
        ZobristHashing.recalculateHash(this)
    }

    // assumes the move is legal, no verification
    fun movePiece(move: Move) {
        with(move) {
            // check king (optimization)
            val targetPiece = pieces[to.x][to.y]
            if(targetPiece.isKing()) {
                if(targetPiece.isWhite()) {
                    whiteKingTaken = true
                } else if(targetPiece.isBlack()) {
                    blackKingTaken = true
                }
            }

            // adapt hash of taken target piece, NONE has an indicator of 0L
            hash = hash.xor(ZobristTable.pieceIndicator[to.x][to.y][targetPiece])
            // adapt hash of source piece
            hash = hash.xor(ZobristTable.pieceIndicator[from.x][from.y][pieces[from.x][from.y]])

            // move
            pieces[to.x][to.y] = pieces[from.x][from.y]
            pieces[from.x][from.y] = NONE

            enPassantTarget?.let {
                pieces[it.x][it.y] = NONE
            }

            // conversion, TODO: this only supports queens at the moment, add conversion target to move obj
            if((to.y == 0 || to.y == 7) && pieces[to.x][to.y].isPawn()) {
                pieces[to.x][to.y] = QUEEN_MASK + pieces[to.x][to.y].player.mask
            }

            // set as moved
            pieces[to.x][to.y] = pieces[to.x][to.y].or(MOVED_MASK)

            // adapt hash to new piece
            hash = hash.xor(ZobristTable.pieceIndicator[to.x][to.y][pieces[to.x][to.y]])

            // castle
            if(pieces[to.x][to.y].isKing()) {
                castleRookMove?.let {
                    movePiece(it)
                }
            }
        }

        lastMove = move

        hash = hash.xor(ZobristTable.blackMoveIndicator)
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

    fun clone() = Board(this)

    fun getPossibleMoves(player: Player) = getPossibleMovesForBoard(this, player)
    fun getPossibleMovesForPiece(coords: Coords) = getPossibleMovesForPiece(this, piece(coords).player, coords, piece(coords))

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

    println("\n" + Evaluator(board).evaluate())
}
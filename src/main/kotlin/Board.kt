import Player.WHITE
import engine.Evaluator
import optimization.ZobristHashing
import optimization.ZobristTable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class Board(init: Boolean = true, val size: Int = 8, var boardPool: BoardPool = BoardPool()) {

    // TODO: when is the BoardPool constructor called? Always or only when used?

    val pieces: Array<Array<Int>> = Array(8) { Array(8) { NONE } }
    var lastMove: Move? = null

    var hash: Long = 0L

    val squares: ArrayList<Coords> get() = Board.squares

    // optimization
    var whiteKingTaken = false
        private set
    var blackKingTaken = false
        private set
    val kingTaken get() = whiteKingTaken || blackKingTaken

    constructor(src: Board, boardPool: BoardPool = BoardPool()) : this(init = false, boardPool = boardPool) {
        initReusedBoard(src)
    }

    fun initReusedBoard(src: Board) {
        squares.forEach {
            pieces[it.x][it.y] = src.pieces[it.x][it.y]
        }
        // TODO: reduce 2D to 1D array
//        System.arraycopy(src.pieces, 0, pieces, 0, size * size)

        lastMove = src.lastMove

        hash = src.hash

        whiteKingTaken = src.whiteKingTaken
        blackKingTaken = src.blackKingTaken
    }

    companion object {
        val squares = ArrayList<Coords>().apply {
            for(x in 0 until 8) {
                for(y in 0 until 8) {
                    add(Coords(x, y))
                }
            }
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

    fun clone() = boardPool.getBoard(this)
    fun cloneWithNewPool() = Board(this, boardPool = BoardPool())
    fun free() = boardPool.free(this)

    fun getPossibleMoves(player: Player) = getPossibleMovesForBoard(this, player)
    fun getPossibleMovesForPiece(coords: Coords) = getPossibleMovesForPiece(this, piece(coords).player, coords, piece(coords))

    fun piece(coords: Coords) = pieces[coords.x][coords.y]
    fun empty(coords: Coords) = piece(coords) == NONE

    fun indicesX() = 0 until size
    fun indicesY() = 0 until size

    // NOT THREAD SAFE!
    class BoardPool() {

//        val pool = HashSet<Board>()
        val available: Queue<Board> = LinkedList()
        val assigned = HashSet<Board>()

        fun getBoard(src: Board): Board {
            if(available.isNotEmpty()) {
                val availableBoard = available.poll()
                assigned.add(availableBoard)
                availableBoard.initReusedBoard(src)
                return availableBoard
            } else {
                val newBoard = Board(src, boardPool = this)
//                pool.add(newBoard)
                assigned.add(newBoard)
                return newBoard
            }
        }

        fun free(board: Board) {
            assigned.remove(board)
            available.add(board)
        }

    }
}

fun main(args: Array<String>) {
    val board = Board()
    board.println()

    val possibleMoves = board.getPossibleMoves(WHITE)
    println("\nPossible moves: ${possibleMoves.size}")
    possibleMoves.forEach { println(it) }

    println("\n" + Evaluator(board).evaluate())
}
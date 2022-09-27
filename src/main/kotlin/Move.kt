import util.math.Vector
import kotlin.math.abs
import kotlin.math.absoluteValue

data class Move(val from: Coords, val to: Coords, val player: Player, val enPassantTarget: Coords? = null, val castleRookMove: Move? = null) {
    override fun toString() = "$from -> $to"

    fun isInBounds(board: Board) = from.isInBounds(board) && to.isInBounds(board)

    fun isTargetFreeOrOpponent(board: Board) = board.empty(to) || !board.piece(to).isPlayerColor(player)

    // Does not check pawn move validity!!!
    fun isValid(board: Board) = isInBounds(board) && isPathFree(board) && isTargetFreeOrOpponent(board)

    fun isPathFree(board: Board): Boolean {
        val vec = to - from

        if(abs(vec.x) <= 1 && abs(vec.y) <= 1) {
            return true
        }

        val rookOrQueen = vec.x == 0 || vec.y == 0  // will also include pawn on initial 2 square move
        val bishopOrQueen = abs(vec.x) == abs(vec.y)
        if(rookOrQueen || bishopOrQueen) {
            val vecNormalized = vec.sign()
//            assert(vecNormalized.x.absoluteValue == 1 && vecNormalized.y.absoluteValue == 1)

            var pos = from

            for(i in 0 until board.size) {
                pos += vecNormalized

                if(pos == to) {
                    break
                }

                if(! board.empty(pos)) {
                    return@isPathFree false
                }
            }
        }

        return true
    }
}

val possibleOffsetsPawnWhite = listOf(
    Coords(0, 1)
)
val possibleOffsetsBasePawnWhite = listOf(
    Coords(0, 1),
    Coords(0, 2),
)
val possibleAttackOffsetsPawnWhite = listOf(
    Coords(-1, 1),
    Coords( 1, 1)
)
val possibleOffsetsPawnBlack = possibleOffsetsPawnWhite.map { Coords(it.x, -it.y) }
val possibleOffsetsBasePawnBlack = possibleOffsetsBasePawnWhite.map { Coords(it.x, -it.y) }
val possibleAttackOffsetsPawnBlack = possibleAttackOffsetsPawnWhite.map { Coords(it.x, -it.y) }


val possibleOffsetsKnight = listOf(
    Coords( 1,  2),
    Coords( 2,  1),
    Coords(-1,  2),
    Coords(-2,  1),
    Coords( 1, -2),
    Coords( 2, -1),
    Coords(-1, -2),
    Coords(-2, -1)
)

val possibleOffsetsBishop = ArrayList<Coords>().apply {
    for(dist in 1..8) {
        add(Coords( dist,  dist))
        add(Coords(-dist,  dist))
        add(Coords( dist, -dist))
        add(Coords(-dist, -dist))
    }
}

val possibleOffsetsRook = ArrayList<Coords>().apply {
    for(dist in 1..8) {
        add(Coords( dist,   0))
        add(Coords(-dist,   0))
        add(Coords(  0,  dist))
        add(Coords(  0, -dist))
    }
}

val possibleOffsetsQueen = ArrayList<Coords>(possibleOffsetsBishop.union(possibleOffsetsRook))
val possibleOffsetsKing = listOf(
    Coords( 1,  0),
    Coords( 1,  1),
    Coords( 0,  1),
    Coords(-1,  1),
    Coords(-1,  0),
    Coords(-1, -1),
    Coords( 0, -1),
    Coords( 1, -1),
)

fun getPossibleMovesForBoard(board: Board, player: Player): List<Move> {
    val possibleMoves = ArrayList<Move>()
    for(square in board.squares) {
        val piece = board.piece(square)
        if(piece != NONE && piece.isPlayerColor(player)) {
            possibleMoves.addAll(getPossibleMovesForPiece(board, player, square, piece))
        }
    }

    return possibleMoves
}

fun getPossibleMovesForPiece(board: Board, player: Player, coords: Coords, piece: Int): List<Move> {
    val possibleMoves = ArrayList<Move>()

    if(! piece.isPlayerColor(player)) {
        return emptyList()
    }

    if(piece.isKnight()) {
        possibleOffsetsKnight.forEach { possibleMoves.add(Move(coords, coords + it, player)) }
    } else if(piece.isBishop()) {
        possibleOffsetsBishop.forEach { possibleMoves.add(Move(coords, coords + it, player)) }
    } else if(piece.isRook()) {
        possibleOffsetsRook.forEach { possibleMoves.add(Move(coords, coords + it, player)) }
    } else if(piece.isQueen()) {
        possibleOffsetsQueen.forEach { possibleMoves.add(Move(coords, coords + it, player)) }
    } else if(piece.isKing()) {
        possibleOffsetsKing.forEach { possibleMoves.add(Move(coords, coords + it, player)) }

        // castle
        if(!piece.moved()) {
            val leftRookCoords = Coords(0, coords.y) // this is relative to playing black, as the board is rotated this way in memory
            val rightRookCoords = Coords(7, coords.y)
            val leftRook = board.piece(leftRookCoords)
            val rightRook = board.piece(rightRookCoords)

            if(leftRook.isRook() && leftRook.player == piece.player && !leftRook.moved()) {
                val squaresBetween = listOf(Coords(1, coords.y), Coords(2, coords.y))
                val allRelevantSquares = squaresBetween.union(listOf(coords, leftRookCoords))

                if(squaresBetween.all { board.empty(it) }) {
                    //TODO: check check
                    possibleMoves.add(Move(
                        coords, Coords(1, coords.y), player,
                        castleRookMove = Move(leftRookCoords, Coords(2, coords.y), player)
                    ))
                }
            }
            if(rightRook.isRook() && rightRook.player == piece.player && !rightRook.moved()) {
                val squaresBetween = listOf(Coords(4, coords.y), Coords(5, coords.y), Coords(6, coords.y))
                val allRelevantSquares = squaresBetween.union(listOf(coords, rightRookCoords))

                if(squaresBetween.all { board.empty(it) }) {
                    //TODO: check check
                    possibleMoves.add(Move(
                        coords, Coords(5, coords.y), player,
                        castleRookMove = Move(rightRookCoords, Coords(4, coords.y), player)
                    ))
                }
            }
        }



    } else if(piece.isPawn()) {
        if((piece.isWhite() && coords.y == 1) || (piece.isBlack() && coords.y == 6)) {
            (if(piece.isWhite()) possibleOffsetsBasePawnWhite else possibleOffsetsBasePawnBlack).forEach {
                val target = coords + it

                if(target.isInBounds(board) && board.empty(target)) {
                    possibleMoves.add(Move(coords, target, player))
                }
            }
        } else {
            (if(piece.isWhite()) possibleOffsetsPawnWhite else possibleOffsetsPawnBlack).forEach {
                val target = coords + it

                if(target.isInBounds(board) && board.empty(target)) {
                    possibleMoves.add(Move(coords, target, player))
                }
            }
        }

        (if(piece.isWhite()) possibleAttackOffsetsPawnWhite else possibleAttackOffsetsPawnBlack).forEach {
            val target = coords + it

            if(target.isInBounds(board) && !board.empty(target) && board.piece(target).isPlayerColor(player.otherPlayer)) {
                possibleMoves.add(Move(coords, target, player))
            }
        }

        // en passant
        (if(piece.isWhite()) possibleAttackOffsetsPawnWhite else possibleAttackOffsetsPawnBlack).forEach {
            val movementTarget = coords + it
            val opponentTarget = coords + Coords(it.x, 0)

            if(movementTarget.isInBounds(board) && opponentTarget.isInBounds(board)
                && !board.empty(opponentTarget) && board.piece(opponentTarget).isPlayerColor(player.otherPlayer)
                && board.lastMove?.to == opponentTarget && board.piece(opponentTarget).isPawn()
                && board.lastMove?.run { abs(to.y - from.y) } == 2
                && ((piece.isWhite() && coords.y == 4) || (piece.isBlack() && coords.y == 3))
            ) {
                possibleMoves.add(Move(coords, movementTarget, player, enPassantTarget = opponentTarget))
            }
        }
    }

    possibleMoves.removeIf { ! it.isValid(board) }
    return possibleMoves
}

import kotlin.math.abs
import kotlin.math.absoluteValue

data class Move(val from: Coords, val to: Coords, val player: Player) {
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

        val rookOrQueen = vec.x == 0 || vec.y == 0
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
val possibleAttackOffsetsPawnWhite = listOf(
    Coords(-1, 1),
    Coords( 1, 1)
)
val possibleOffsetsPawnBlack = possibleOffsetsPawnWhite.map { Coords(it.x, -it.y) }
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

        // TODO: castle
    } else if(piece.isPawn()) {
        (if(piece.isWhite()) possibleOffsetsPawnWhite else possibleOffsetsPawnBlack).forEach {
            possibleMoves.add(Move(coords, coords + it, player))
        }

        (if(piece.isWhite()) possibleAttackOffsetsPawnWhite else possibleAttackOffsetsPawnBlack).forEach {
            val target = coords + it

            if(target.isInBounds(board) && !board.empty(target) && board.piece(target).isPlayerColor(player.otherPlayer)) {
                possibleMoves.add(Move(coords, target, player))
            }
        }

        // TODO: en passant
    }

    possibleMoves.removeIf { ! it.isValid(board) }
    return possibleMoves
}

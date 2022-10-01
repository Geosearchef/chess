import java.util.*

val NONE: Int = 0b00000000

val MOVED_MASK: Int = 0b10000000

val PAWN_MASK: Int = 0b00000001
val KNIGHT_MASK: Int = 0b00000010
val BISHOP_MASK: Int = 0b00000100
val ROOK_MASK: Int = 0b00001000
val QUEEN_MASK: Int = 0b00010000
val KING_MASK: Int = 0b00100000

val PIECE_MASKS = listOf(
    PAWN_MASK, KNIGHT_MASK, BISHOP_MASK, ROOK_MASK, QUEEN_MASK, KING_MASK
)

val PAWN_WHITE: Int = PAWN_MASK + Player.WHITE.mask
val KNIGHT_WHITE: Int = KNIGHT_MASK + Player.WHITE.mask
val BISHOP_WHITE: Int = BISHOP_MASK + Player.WHITE.mask
val ROOK_WHITE: Int = ROOK_MASK + Player.WHITE.mask
val QUEEN_WHITE: Int = QUEEN_MASK + Player.WHITE.mask
val KING_WHITE: Int = KING_MASK + Player.WHITE.mask

val PAWN_BLACK: Int = PAWN_MASK + Player.BLACK.mask
val KNIGHT_BLACK: Int = KNIGHT_MASK + Player.BLACK.mask
val BISHOP_BLACK: Int = BISHOP_MASK + Player.BLACK.mask
val ROOK_BLACK: Int = ROOK_MASK + Player.BLACK.mask
val QUEEN_BLACK: Int = QUEEN_MASK + Player.BLACK.mask
val KING_BLACK: Int = KING_MASK + Player.BLACK.mask

val KNIGHT_BISHOP_QUEEN_MASK = KNIGHT_MASK.or(BISHOP_MASK).or(QUEEN_MASK)
val WHITE_KING_MASK = Player.WHITE.mask.or(KING_MASK)
val BLACK_KING_MASK = Player.BLACK.mask.or(KING_MASK)

fun Int.getPieceRepresentation(): String {
    var key = "."
    if(this.isPawn()) {
        key = "p"
    } else if (this.isKnight()) {
        key = "n"
    } else if (this.isBishop()) {
        key = "b"
    } else if (this.isRook()) {
        key = "r"
    } else if (this.isQueen()) {
        key = "q"
    } else if (this.isKing()) {
        key = "k"
    }

    if(Player.BLACK.mask.and(this) != 0) {
        key = key.uppercase(Locale.getDefault())
    } else {
        key = key.lowercase(Locale.getDefault())
    }

    return key
}

fun Int.isPawn() = PAWN_MASK.and(this) != 0
fun Int.isKnight() = KNIGHT_MASK.and(this) != 0
fun Int.isBishop() = BISHOP_MASK.and(this) != 0
fun Int.isRook() = ROOK_MASK.and(this) != 0
fun Int.isQueen() = QUEEN_MASK.and(this) != 0
fun Int.isKing() = KING_MASK.and(this) != 0

fun Int.isWhite() = !this.isBlack()
fun Int.isBlack() = Player.BLACK.mask.and(this) != 0
fun Int.isPlayerColor(player: Player) = if(player == Player.WHITE) this.isWhite() else this.isBlack()
val Int.player get() = if(this.isWhite()) Player.WHITE else Player.BLACK

fun Int.moved() = MOVED_MASK.and(this) != 0


// optimization
fun Int.isKnightBishopOrQueen() = this.and(KNIGHT_BISHOP_QUEEN_MASK) != 0
fun Int.isWhiteKing() = this.and(WHITE_KING_MASK) != 0
fun Int.isBlackKing() = this.and(BLACK_KING_MASK) != 0


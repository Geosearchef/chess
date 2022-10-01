package optimization

import MOVED_MASK
import PIECE_MASKS
import java.security.SecureRandom
import kotlin.random.Random

object ZobristTable {

    val pieceIndicator = Array(8) { Array(8) { Array(256) { 0L } } }
    var blackMoveIndicator = 0L

    // no indication for castling rights, as this is encoded in the piece (indicator)

    val validEnPassantFile = Array(8) { 0L }  // (column)


    val seed = 0L
    val random = Random(seed)  // TODO: use higher quality random numbers!


    init {
        // 1536 values generated
        for(x in 0 until 8) {
            for(y in 0 until 8) {
                for(colorMask in listOf(Player.WHITE.mask, Player.BLACK.mask)) {
                    for(movedMask in listOf(MOVED_MASK, 0)) {
                        for(pieceType in PIECE_MASKS) {
                            val piece = pieceType + colorMask + movedMask

                            pieceIndicator[x][y][piece] = random.nextLong()
                        }
                    }
                }
            }
        }

        blackMoveIndicator = random.nextLong()
        for(x in 0 until 8) {
            validEnPassantFile[x] = random.nextLong()
        }
    }
}
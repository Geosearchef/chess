package optimization

import Board
import Player

object ZobristHashing {

    // this is actually optional, as we only care about progress
    fun recalculateHash(board: Board, playerToMove: Player = Player.WHITE) {
        var hash: Long = 0L
        for(square in board.squares) {
            hash = hash.xor(ZobristTable.pieceIndicator[square.x][square.y][board.piece(square)])
        }

        if(playerToMove == Player.BLACK) {
            hash = hash.xor(ZobristTable.blackMoveIndicator)
        }

        // warning: this ignores en passant

        board.hash = hash
    }
}
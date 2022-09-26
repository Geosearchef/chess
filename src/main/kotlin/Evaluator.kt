
class Evaluator(val board: Board) {
    fun evaluate(): Double {
        var score = 0.0

        for(square in board.squares) {
            if(board.empty(square)) {
                continue
            }

            val piece = board.piece(square)
            var pieceScore = 0.0
            if(piece.isPawn()) {
                pieceScore = 1.0
            } else if(piece.isKnight()) {
                pieceScore = 3.0
            } else if(piece.isBishop()) {
                pieceScore = 3.0
            } else if(piece.isRook()) {
                pieceScore = 5.0
            } else if(piece.isQueen()) {
                pieceScore = 9.0
            } else if(piece.isKing()) {
                pieceScore = 200.0  // max sum of all others is 103
            }

            if(piece.isWhite()) {
                score += pieceScore
            } else {
                score -= pieceScore
            }
        }

        return score
    }
}


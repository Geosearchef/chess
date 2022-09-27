package engine

import BISHOP_MASK
import Board
import KNIGHT_MASK
import QUEEN_MASK
import isBishop
import isBlack
import isKing
import isKnight
import isKnightBishopOrQueen
import isPawn
import isQueen
import isRook
import isWhite

const val MOBILITY_FACTOR = 0.05
const val DEVELOPMENT_FACTOR = 1.0
const val PAWN_FACTOR = 1.0

class Evaluator(val board: Board) {
    fun evaluate(): Double {
        var score = 0.0

        score += evaluateMaterial()
        score += evaluatePawns() * PAWN_FACTOR
        score += evaluateMobility() * MOBILITY_FACTOR
        score += evaluateDevelopment() * DEVELOPMENT_FACTOR

        return score
    }

    fun evaluatePawns(): Double {
        // how are the pawns covered
        var score = 0.0

        for(square in board.squares) {
            val piece = board.piece(square)
            if(piece.isPawn()) {
                if(piece.isWhite()) {
                    score += (square.y - 1) * 0.2
                } else {
                    score -= (6 - square.y) * 0.2
                }
            }
        }

        // subtract blocked pawns

        return score
    }

    fun evaluateMobility(): Double {
//        return (board.getPossibleMoves(Player.WHITE).size - board.getPossibleMoves(Player.BLACK).size).toDouble()
        return 0.0
    }

    fun evaluateDevelopment(): Double {
        var developmentScore = 0.0

        for(square in board.squares) {
            val piece = board.piece(square)
            if(piece.isKnightBishopOrQueen()) {
                if(piece.isWhite() && square.y != 0) {
                    developmentScore += 0.5
                } else if(piece.isBlack() && square.y != 7) {
                    developmentScore -= 0.5
                }
            }

            // capture center with pawns
            if(piece.isPawn() && (square.x == 3 || square.x == 4) && (square.y == 3 || square.y == 4)) {
                if(piece.isWhite()) {
                    developmentScore += 0.6
                } else {
                    developmentScore -= 0.6
                }
            }
        }

        // add available castle
        // add executed castle

        return developmentScore
    }

    fun evaluateMaterial(): Double {
        var materialScore = 0.0

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
                materialScore += pieceScore
            } else {
                materialScore -= pieceScore
            }
        }

        return materialScore
    }
}


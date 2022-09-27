package engine

import Board
import Move
import Player
import java.util.stream.Collectors


fun calculateOptimalScore(board: Board, playerToMove: Player, iterationDepth: Int = 5): Double {
    if(iterationDepth <= 0) {
        return Evaluator(board).evaluate()
    }

    val possibleScoresByMove = calculateMoveRanking(board, playerToMove, iterationDepth)

    if(playerToMove == Player.WHITE) {
        return possibleScoresByMove.values.maxOrNull() ?: 0.0
    } else {
        return possibleScoresByMove.values.minOrNull() ?: 0.0
    }
}

fun calculateMoveRanking(board: Board, playerToMove: Player, iterationDepth: Int = 5, parallel: Boolean = false): Map<Move, Double> {
    val otherPlayer = playerToMove.otherPlayer

    val possibleMoves = board.getPossibleMoves(playerToMove)

    if(!parallel) {
        val possibleScoresByMove = possibleMoves.associateWith { move ->
            val newBoard = board.clone()
            newBoard.movePiece(move)
            calculateOptimalScore(newBoard, otherPlayer, iterationDepth - 1)
        }

        return possibleScoresByMove
    } else {
        val possibleScores = possibleMoves.parallelStream().map { move ->
            val newBoard = board.clone()
            newBoard.movePiece(move)
            calculateOptimalScore(newBoard, otherPlayer, iterationDepth - 1)
        }.collect(Collectors.toList())

        val possibleScoresByMove = possibleMoves.indices.associate { possibleMoves[it] to possibleScores[it] }

        return possibleScoresByMove
    }
}


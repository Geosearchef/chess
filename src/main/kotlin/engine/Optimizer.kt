package engine

import Board
import Move
import Player
import java.util.stream.Collectors


fun calculateOptimalScore(board: Board, playerToMove: Player, iterationDepth: Int = 5): Double {
    if(iterationDepth <= 0) {
        return Evaluator(board).evaluate()
    }

    // optimization - abort branch
    if(board.kingTaken) {
        return Evaluator(board).evaluate() * (iterationDepth.toDouble() + 1.0) // prefer earlier checkmates, avoid stalling
    }

    val possibleScoresByMove = calculateMoveRanking(board, playerToMove, iterationDepth)

    if(playerToMove == Player.WHITE) {
        return possibleScoresByMove.values.maxOrNull() ?: 0.0
    } else {
        return possibleScoresByMove.values.minOrNull() ?: 0.0
    }
}

fun calculateMoveRanking(board: Board, playerToMove: Player, iterationDepth: Int = 5, parallel: Boolean = false, allowedInitialMoves: List<Move>? = null): Map<Move, Double> {
    val otherPlayer = playerToMove.otherPlayer

    val possibleMoves = allowedInitialMoves ?: board.getPossibleMoves(playerToMove)

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

fun calculateMoveRankingIteratively(board: Board, playerToMove: Player, iterationDepths: List<Int> = listOf(4, 6, 8), parallel: Boolean = false): Map<Move, Double> {
    var allowedInitialMoves = board.getPossibleMoves(playerToMove)
    var lastRanking: Map<Move, Double> = mapOf()

    println("\nCalculating move ranking...")
    val startTimeAll = System.currentTimeMillis()

    for(iterationDepth in iterationDepths) {
        val startTimeIteration = System.currentTimeMillis()

        val ranking = calculateMoveRanking(board, playerToMove, iterationDepth, parallel, allowedInitialMoves)
        val bestScore = if(playerToMove == Player.WHITE) ranking.values.maxOrNull() else ranking.values.minOrNull()
        val bestMoves = ranking.filterValues { it == bestScore }.keys

        allowedInitialMoves = bestMoves.toList()

        lastRanking = ranking

        println("Depth: $iterationDepth, t: ${System.currentTimeMillis() - startTimeIteration} ms, remaining: ${bestMoves.size}")

        if(bestMoves.size <= 1) {
            break
        }
    }

    println("Done. Took ${System.currentTimeMillis() - startTimeAll} ms.\n")

    return lastRanking
}

fun calculateOptimalMovesIteratively(board: Board, playerToMove: Player, iterationDepths: List<Int> = listOf(4, 6, 8), parallel: Boolean = false): Pair<Set<Move>, Double?> {
    val ranking = calculateMoveRankingIteratively(board, playerToMove, iterationDepths, parallel)
    val bestScore = if(playerToMove == Player.WHITE) ranking.values.maxOrNull() else ranking.values.minOrNull()
    val bestMoves = ranking.filterValues { it == bestScore }.keys

    return bestMoves to bestScore
}


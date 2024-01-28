package engine

import Board
import Move
import Player
import optimization.SharedAlphaBeta
import optimization.TranspositionTable
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.min


fun calculateOptimalScore(
    board: Board,
    playerToMove: Player,
    transpositionTable: TranspositionTable,
    iterationDepth: Int = 5,
    alpha: Double,
    beta: Double,
    sharedAlphaBeta: SharedAlphaBeta? = null
): Double {
    if(iterationDepth <= 0) {
        return Evaluator(board).evaluate()
    }

    // optimization - abort branch
    if(board.kingTaken) {
        return Evaluator(board).evaluate() * (iterationDepth.toDouble() + 1.0) // prefer earlier checkmates, avoid stalling
    }

    transpositionTable.lookup(board, iterationDepth)?.let { transposition ->
        return@calculateOptimalScore transposition.score
    }

    val possibleScoresByMove = calculateMoveRanking(board, playerToMove, transpositionTable, iterationDepth, alpha = alpha, beta = beta, sharedAlphaBeta = sharedAlphaBeta)

    var optimalScore = 0.0
    if(playerToMove == Player.WHITE) {
        optimalScore = possibleScoresByMove.values.maxOrNull() ?: 0.0
    } else {
        optimalScore = possibleScoresByMove.values.minOrNull() ?: 0.0
    }

    transpositionTable.put(board, iterationDepth, optimalScore)

    return optimalScore
}

fun calculateMoveRanking(
    board: Board,
    playerToMove: Player,
    transpositionTable: TranspositionTable,
    iterationDepth: Int = 5,
    parallel: Boolean = false,
    allowedInitialMoves: List<Move>? = null,
    alpha: Double = -100000.0,
    beta: Double = +100000.0,
    sharedAlphaBeta: SharedAlphaBeta? = null
): Map<Move, Double> {
    val otherPlayer = playerToMove.otherPlayer

    val possibleMoves = allowedInitialMoves ?: board.getPossibleMoves(playerToMove).sortedBy { -it.alphaBetaPriority(board) }

    if(!parallel) {
        val possibleScoresByMove = HashMap<Move, Double>()

        var currentAlpha = alpha
        var currentBeta = beta

        for (move in possibleMoves) {
            val newBoard = board.clone()
            newBoard.movePiece(move)
            val optimalScore = calculateOptimalScore(newBoard, otherPlayer, transpositionTable, iterationDepth - 1, currentAlpha, currentBeta)
            newBoard.free()
            possibleScoresByMove[move] = optimalScore

            if(playerToMove == Player.WHITE) {
                currentAlpha = max(currentAlpha, optimalScore)
            } else {
                currentBeta = min(currentBeta, optimalScore)
            }

            // only executed on the level below the parallel level
//            sharedAlphaBeta?.update(currentAlpha, currentBeta)
//            sharedAlphaBeta?.let {
//                currentAlpha = max(currentAlpha, it.alpha)
//                currentBeta = min(currentBeta, it.beta)
//            }

            if(currentAlpha > currentBeta) {
                break
            }
        }

//        val possibleScoresByMove = possibleMoves.associateWith { move ->
//            val newBoard = board.clone()
//            newBoard.movePiece(move)
//            val optimalScore = calculateOptimalScore(newBoard, otherPlayer, transpositionTable, iterationDepth - 1)
//            newBoard.free()
//            return@associateWith optimalScore
//        }

        return possibleScoresByMove
    } else {
        val sharedAlphaBeta = SharedAlphaBeta()

        val possibleScores = possibleMoves.parallelStream().map { move ->
            val newBoard = board.cloneWithNewPool()
            val newTranspositionTable = transpositionTable.clone()
            newBoard.movePiece(move)
            val optimalScore = calculateOptimalScore(newBoard, otherPlayer, newTranspositionTable, iterationDepth - 1, alpha, beta, sharedAlphaBeta)

//            if(move.player == Player.WHITE) {
//                sharedAlphaBeta.update(alpha = optimalScore)
//            } else {
//                sharedAlphaBeta.update(beta = optimalScore)
//            }

            println(newTranspositionTable.toString())
            return@map optimalScore
        }.collect(Collectors.toList())

        val possibleScoresByMove = possibleMoves.indices.associate { possibleMoves[it] to possibleScores[it] }

        return possibleScoresByMove
    }
}

fun calculateMoveRankingIteratively(board: Board, playerToMove: Player, transpositionTable: TranspositionTable, iterationDepths: List<Int> = listOf(4, 6, 8), parallel: Boolean = false): Map<Move, Double> {
    var allowedInitialMoves = board.getPossibleMoves(playerToMove)
    var lastRanking: Map<Move, Double> = mapOf()
    var firstRanking: Map<Move, Double> = mapOf()

    println("\nCalculating move ranking...")
    val startTimeAll = System.currentTimeMillis()

    for(iterationDepth in iterationDepths) {
        val startTimeIteration = System.currentTimeMillis()

        val ranking = calculateMoveRanking(board, playerToMove, transpositionTable, iterationDepth, parallel, allowedInitialMoves)
        val bestScore = if(playerToMove == Player.WHITE) ranking.values.maxOrNull() else ranking.values.minOrNull()
        val bestMoves = ranking.filterValues { it == bestScore }.keys

        allowedInitialMoves = bestMoves.toList()

        if(firstRanking.isEmpty()) {
            firstRanking = ranking
        }

        lastRanking = ranking

        println("Depth: $iterationDepth, t: ${System.currentTimeMillis() - startTimeIteration} ms, remaining: ${bestMoves.size}")

        if(bestMoves.size <= 1) {
            break
        }
    }

    println("Deepest score of best move: ${if(playerToMove == Player.WHITE) firstRanking.values.maxOrNull() else firstRanking.values.minOrNull()}")
    println("Done. Took ${System.currentTimeMillis() - startTimeAll} ms.\n")

    return lastRanking
}

fun calculateOptimalMovesIteratively(board: Board, playerToMove: Player, transpositionTable: TranspositionTable, iterationDepths: List<Int> = listOf(4, 6, 8), parallel: Boolean = false): Pair<Set<Move>, Double?> {
    val ranking = calculateMoveRankingIteratively(board, playerToMove, transpositionTable, iterationDepths, parallel)
    val bestScore = if(playerToMove == Player.WHITE) ranking.values.maxOrNull() else ranking.values.minOrNull()
    val bestMoves = ranking.filterValues { it == bestScore }.keys

    return bestMoves to bestScore
}


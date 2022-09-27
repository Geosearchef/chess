package visualization

import Board
import Coords
import Move
import getPieceRepresentation
import isWhite
import player
import util.math.Vector
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.floor

class BoardVisualizer(val board: Board, val invert: Boolean = false) : JPanel(), MouseListener {

    val SQUARE_SIZE = 80

    val WIDTH = SQUARE_SIZE * 8
    val HEIGHT = SQUARE_SIZE * 8 + 37

    val SQUARE_COLOR_DARK = Color(125, 148, 93, 255)
    val SQUARE_COLOR_LIGHT = Color(238, 238, 213, 255)
    val SELECTOR_COLOR = Color(0, 0, 0, 255)
    val POSSIBLE_MOVE_TARGET_COLOR = Color(255, 197, 102, 255)

    val frame = JFrame("CE").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(WIDTH, HEIGHT)
        setLocationRelativeTo(null)

        add(this@BoardVisualizer)

        isVisible = true
    }

    var selectedSquare: Coords? = null

    companion object {
        val images = HashMap<String, BufferedImage>()
        val IMAGE_PATH = Paths.get("./res/pieces")

        init {
            Files.list(IMAGE_PATH).forEach {
                images.put(it.fileName.toString(), ImageIO.read(it.toFile()))
            }
        }
    }

    override fun paintComponent(g: Graphics?) {
        with(g as Graphics2D) {
            clearRect(0, 0, WIDTH, HEIGHT)

            for(square in board.squares) {
                if((square.x + square.y) % 2 == 0) {
                    color = SQUARE_COLOR_LIGHT
                } else {
                    color = SQUARE_COLOR_DARK
                }

                val screenSpacePos = boardToScreenSpace(square)
                fillRect(screenSpacePos, SQUARE_SIZE, SQUARE_SIZE)

                if(selectedSquare == square) {
                    color = SELECTOR_COLOR
                    val oldStroke = stroke
                    stroke = BasicStroke(4f) // this is a basic stroke with a width of three, hf pronouncing that
                    drawRect(screenSpacePos + Vector(2.0, 2.0), SQUARE_SIZE - 4, SQUARE_SIZE - 4)
                    stroke = oldStroke
                }

                if(!board.empty(square)) {
                    val piece = board.piece(square)
                    val img = images["${piece.getPieceRepresentation().lowercase()}${if(piece.isWhite()) "l" else "d"}t.png"]
                    img?.let {
                        drawImage(it, screenSpacePos)
                    }
                }
            }

            // render possible moves
            selectedSquare?.let { selectedSquare ->
                val possibleMoves = board.getPossibleMovesForPiece(selectedSquare)

                color = POSSIBLE_MOVE_TARGET_COLOR
                val oldStroke = stroke
                stroke = BasicStroke(4f) // this is a basic stroke with a width of three, hf pronouncing that
                possibleMoves.forEach {
                    drawRect(boardToScreenSpace(it.to) + Vector(2.0, 2.0), SQUARE_SIZE - 4, SQUARE_SIZE - 4)
                }
                stroke = oldStroke
            }
        }
    }




    init {
        this.addMouseListener(this)
    }

    override fun mousePressed(e: MouseEvent?) {
        e ?: throw RuntimeException("AWT encountered an error while handing us a mouse event.")

        val clickedSquare = screenToBoardSpace(Vector(e.x.toDouble(), e.y.toDouble()))

        if(selectedSquare == null) {
            selectedSquare = clickedSquare
        } else if (selectedSquare == clickedSquare) {
            selectedSquare = null
        } else {
            selectedSquare?.let {
                if(board.empty(it)) {
                    selectedSquare = clickedSquare
                } else {
                    // execute the move
                    board.movePiece(Move(it, clickedSquare, board.piece(it).player))
                }
            }
        }

        requestRepaint()
    }
    override fun mouseReleased(e: MouseEvent?) {}
    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseEntered(e: MouseEvent?) {}
    override fun mouseExited(e: MouseEvent?) {}


    fun requestRepaint() {
        frame.repaint()
    }

    fun boardToScreenSpace(coords: Coords) = Vector(
        (if(invert) coords.x else board.size - 1 - coords.x) * SQUARE_SIZE.toDouble(),
        (if(invert) coords.y else board.size - 1 - coords.y) * SQUARE_SIZE.toDouble()
    )
    fun screenToBoardSpace(v: Vector) = Coords(
        if(invert) floor(v.x / SQUARE_SIZE).toInt() else board.size - 1 - floor(v.x / SQUARE_SIZE).toInt(),
        if(invert) floor(v.y / SQUARE_SIZE).toInt() else board.size - 1 - floor(v.y / SQUARE_SIZE).toInt()
    )

    fun Graphics2D.drawRect(v: Vector, width: Int, height: Int) = drawRect(v.x.toInt(), v.y.toInt(), width, height)
    fun Graphics2D.fillRect(v: Vector, width: Int, height: Int) = fillRect(v.x.toInt(), v.y.toInt(), width, height)
    fun Graphics2D.drawImage(img: BufferedImage, v: Vector) = drawImage(img, v.x.toInt(), v.y.toInt(), null)

}


fun main(args: Array<String>) {
    val board = Board()
    val visualizer = BoardVisualizer(board)

    visualizer.requestRepaint()
}


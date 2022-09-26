package visualization

import Board
import Coords
import getPieceRepresentation
import isWhite
import util.math.Vector
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel

class BoardVisualizer(val board: Board, val invert: Boolean = false) : JPanel() {

    val SQUARE_SIZE = 80

    val WIDTH = SQUARE_SIZE * 8
    val HEIGHT = SQUARE_SIZE * 8 + 37

    val SQUARE_COLOR_DARK = Color(125, 148, 93, 255)
    val SQUARE_COLOR_LIGHT = Color(238, 238, 213, 255)

    val frame = JFrame("CE").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(WIDTH, HEIGHT)
        setLocationRelativeTo(null)

        add(this@BoardVisualizer)

        isVisible = true
    }

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

                if(!board.empty(square)) {
                    val piece = board.piece(square)
                    val img = images["${piece.getPieceRepresentation().lowercase()}${if(piece.isWhite()) "l" else "d"}t.png"]
                    img?.let {
                        drawImage(it, screenSpacePos)
                    }
                }
            }
        }
    }

    fun requestRepaint() {
        frame.repaint()
    }

    fun boardToScreenSpace(coords: Coords) = Vector((if(invert) coords.x else board.size - 1 - coords.x) * SQUARE_SIZE.toDouble(), (if(invert) coords.y else board.size - 1 - coords.y) * SQUARE_SIZE.toDouble())
    fun Graphics2D.fillRect(v: Vector, width: Int, height: Int) = fillRect(v.x.toInt(), v.y.toInt(), width, height)
    fun Graphics2D.drawImage(img: BufferedImage, v: Vector) = drawImage(img, v.x.toInt(), v.y.toInt(), null)
}


fun main(args: Array<String>) {
    val board = Board()
    val visualizer = BoardVisualizer(board)

    visualizer.requestRepaint()
}


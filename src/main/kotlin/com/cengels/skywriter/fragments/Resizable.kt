package com.cengels.skywriter.fragments

import com.cengels.skywriter.util.isAnyOf
import javafx.geometry.Point2D
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.lang.Double.max
import java.lang.IllegalArgumentException

/** Adds resizable behaviour to an undecorated stage. */
class Resizable(val stage: Stage) {
    companion object {
        /** The number of pixels just inside the outer window that the cursor can resize the window in. */
        var RESIZE_THRESHOLD: Double = 6.0
    }
    private var resizing = false
    private var canResize = false
    private var resizeStartPosition: Point2D? = null
    private var resizeStartSize: Point2D? = null

    init {
        if (stage.style !== StageStyle.TRANSPARENT && stage.style !== StageStyle.UNDECORATED) {
            throw IllegalArgumentException("Specified stage is not undecorated!")
        }

        stage.addEventFilter(MouseEvent.MOUSE_MOVED) {
            canResize = this.detectIsCursorAtEdge(it)

            if (canResize) {
                it.consume()
            }
        }

        stage.addEventFilter(MouseEvent.MOUSE_PRESSED) {
            if (canResize) {
                resizing = true
                resizeStartPosition = Point2D(it.screenX, it.screenY)
                resizeStartSize = Point2D(stage.width, stage.height)
                it.consume()
            }
        }
        stage.addEventFilter(MouseEvent.MOUSE_RELEASED) {
            if (resizing) {
                resizing = false
                it.consume()
            }
        }
        stage.addEventFilter(MouseEvent.MOUSE_DRAGGED) {
            if (resizing) {
                resize(it)
                it.consume()
            }
        }
        stage.addEventFilter(MouseEvent.MOUSE_EXITED) { if (!resizing) resetCursor() }
        stage.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET) { if (!resizing) resetCursor() }
    }

    private fun resize(mouseEvent: MouseEvent) {
        if (stage.scene.cursor != Cursor.N_RESIZE && stage.scene.cursor != Cursor.S_RESIZE) {
            resizeX(mouseEvent)
        }

        if (stage.scene.cursor != Cursor.W_RESIZE && stage.scene.cursor != Cursor.E_RESIZE) {
            resizeY(mouseEvent)
        }
    }

    private fun resizeX(mouseEvent: MouseEvent) {
        val minWidth = max(stage.minWidth, RESIZE_THRESHOLD * 2)
        val start = resizeStartPosition ?: return
        val size = resizeStartSize ?: return

        if (stage.scene.cursor.isAnyOf(Cursor.NW_RESIZE, Cursor.W_RESIZE, Cursor.SW_RESIZE)) {
            if (stage.width > minWidth || mouseEvent.x < 0) {
                stage.width = (stage.x - mouseEvent.screenX + stage.width).coerceAtLeast(minWidth)
                stage.x = mouseEvent.screenX
            }
        } else {
            if (stage.width > minWidth || mouseEvent.x + start.x - stage.width > 0) {
                stage.width = (mouseEvent.screenX - start.x + size.x).coerceAtLeast(minWidth)
            }
        }
    }

    private fun resizeY(mouseEvent: MouseEvent) {
        val minHeight = max(stage.minHeight, RESIZE_THRESHOLD * 2)
        val start = resizeStartPosition ?: return
        val size = resizeStartSize ?: return

        if (stage.scene.cursor.isAnyOf(Cursor.NW_RESIZE, Cursor.N_RESIZE, Cursor.NE_RESIZE)) {
            if (stage.height > minHeight || mouseEvent.y < 0) {
                stage.height = (stage.y - mouseEvent.screenY + stage.height).coerceAtLeast(minHeight)
                stage.y = mouseEvent.screenY
            }
        } else {
            if (stage.height > minHeight || mouseEvent.y + start.y - stage.height > 0) {
                stage.height = (mouseEvent.screenY - start.y + size.y).coerceAtLeast(minHeight)
            }
        }
    }

    private fun resetCursor() {
        stage.scene.cursor = null
    }

    private fun detectIsCursorAtEdge(mouseEvent: MouseEvent): Boolean {
        if (stage.isMaximized || stage.isFullScreen) {
            return false
        }

        stage.scene.cursor = when {
            mouseEvent.x > stage.width - RESIZE_THRESHOLD * 2 && mouseEvent.y < RESIZE_THRESHOLD * 2 -> Cursor.NE_RESIZE
            mouseEvent.x > stage.width - RESIZE_THRESHOLD * 2 && mouseEvent.y > stage.height - RESIZE_THRESHOLD * 2 -> Cursor.SE_RESIZE
            mouseEvent.x < RESIZE_THRESHOLD * 2 && mouseEvent.y < RESIZE_THRESHOLD * 2 -> Cursor.NW_RESIZE
            mouseEvent.x < RESIZE_THRESHOLD * 2 && mouseEvent.y > stage.height - RESIZE_THRESHOLD * 2 -> Cursor.SW_RESIZE
            mouseEvent.y < RESIZE_THRESHOLD -> Cursor.N_RESIZE
            mouseEvent.x > stage.width - RESIZE_THRESHOLD -> Cursor.E_RESIZE
            mouseEvent.y > stage.height - RESIZE_THRESHOLD -> Cursor.S_RESIZE
            mouseEvent.x < RESIZE_THRESHOLD -> Cursor.W_RESIZE
            else -> null
        }

        return stage.scene.cursor != null
    }
}
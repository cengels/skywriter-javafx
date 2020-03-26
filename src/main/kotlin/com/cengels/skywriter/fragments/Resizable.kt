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
class Resizable(val stage: Stage,
                /** The number of pixels just inside the outer window that the cursor can resize the window in. */
                val threshold: Double = DEFAULT_THRESHOLD) {
    companion object {
        const val DEFAULT_THRESHOLD: Double = 6.0
    }
    /** True if a resize operation is in progress to prevent other UI events from changing the cursor mid-resize, for instance. */
    private var resizing = false
    /** True if the user's cursor is near one of the edges. */
    private var canResize = false
    /** The mouse coordinates at the start of the resize operation. */
    private var resizeStartPosition: Point2D? = null
    /** The stage's size at the start of the resize operation. */
    private var resizeStartSize: Point2D? = null
    /** The stage's position at the start of the resize operation. */
    private var stageStartPosition: Point2D? = null

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
                stageStartPosition = Point2D(stage.x, stage.y)
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
        val minWidth = max(stage.minWidth, threshold * 2)
        val mouseStart = resizeStartPosition ?: return
        val stageStart = stageStartPosition ?: return
        val size = resizeStartSize ?: return

        if (stage.scene.cursor.isAnyOf(Cursor.NW_RESIZE, Cursor.W_RESIZE, Cursor.SW_RESIZE)) {
            if (stage.width > minWidth || mouseEvent.x < 0) {
                stage.width = (size.x + (mouseStart.x - mouseEvent.screenX)).coerceAtLeast(minWidth)
                stage.x = (stageStart.x - mouseStart.x) + mouseEvent.screenX
            }
        } else {
            if (stage.width > minWidth || mouseEvent.x + mouseStart.x - stage.width > 0) {
                stage.width = (mouseEvent.screenX - mouseStart.x + size.x).coerceAtLeast(minWidth)
            }
        }
    }

    private fun resizeY(mouseEvent: MouseEvent) {
        val minHeight = max(stage.minHeight, threshold * 2)
        val mouseStart = resizeStartPosition ?: return
        val stageStart = stageStartPosition ?: return
        val size = resizeStartSize ?: return

        if (stage.scene.cursor.isAnyOf(Cursor.NW_RESIZE, Cursor.N_RESIZE, Cursor.NE_RESIZE)) {
            if (stage.height > minHeight || mouseEvent.y < 0) {
                stage.height = (size.y + (mouseStart.y - mouseEvent.screenY)).coerceAtLeast(minHeight)
                stage.y = (stageStart.y - mouseStart.y) + mouseEvent.screenY
            }
        } else {
            if (stage.height > minHeight || mouseEvent.y + mouseStart.y - stage.height > 0) {
                stage.height = (mouseEvent.screenY - mouseStart.y + size.y).coerceAtLeast(minHeight)
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
            mouseEvent.x > stage.width - threshold * 2 && mouseEvent.y < threshold * 2 -> Cursor.NE_RESIZE
            mouseEvent.x > stage.width - threshold * 2 && mouseEvent.y > stage.height - threshold * 2 -> Cursor.SE_RESIZE
            mouseEvent.x < threshold * 2 && mouseEvent.y < threshold * 2 -> Cursor.NW_RESIZE
            mouseEvent.x < threshold * 2 && mouseEvent.y > stage.height - threshold * 2 -> Cursor.SW_RESIZE
            mouseEvent.y < threshold -> Cursor.N_RESIZE
            mouseEvent.x > stage.width - threshold -> Cursor.E_RESIZE
            mouseEvent.y > stage.height - threshold -> Cursor.S_RESIZE
            mouseEvent.x < threshold -> Cursor.W_RESIZE
            else -> null
        }

        return stage.scene.cursor != null
    }
}
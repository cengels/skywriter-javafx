package com.cengels.skywriter.fragments

import com.cengels.skywriter.style.ThemedStylesheet
import com.cengels.skywriter.svg.Icons
import com.cengels.skywriter.util.SpacedLabel
import com.cengels.skywriter.util.svgbutton
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import tornadofx.*

class ThemedTitlebar(
    val viewTitle: String,
    val showMinimize: Boolean = true,
    val showMaximize: Boolean = true,
    val showClose: Boolean = true,
    val showIcon: Boolean = true
) : Fragment() {
    override val root = borderpane {
        addClass(ThemedStylesheet.titleBar)
        makeDraggable(this)

        left {
            hbox(11) {
                alignment = Pos.CENTER
                if (showIcon) {
                    this += Icons.SKY_WRITER
                }
                this += SpacedLabel(viewTitle, 0.4)
            }
        }

        right {
            hbox {
                if (showMinimize) {
                    svgbutton(Icons.HORIZONTAL_LINE).action { currentStage?.isIconified = true }
                }
                if (showMaximize) {
                    svgbutton(Icons.CORNERLESS_SQUARE).action { currentStage?.isMaximized = !currentStage!!.isMaximized }
                }
                if (showClose) {
                    svgbutton(Icons.X).action { close() }
                }
            }
        }
    }

    private fun makeDraggable(component: Node) {
        var startEventPosition: Point2D? = null
        var startStagePosition: Point2D? = null

        component.addEventHandler(MouseEvent.MOUSE_PRESSED) { event ->
            startEventPosition = Point2D(event.screenX, event.screenY)
            currentStage?.let { startStagePosition = Point2D(it.x, it.y) }
        }

        component.addEventHandler(MouseEvent.MOUSE_DRAGGED) { event ->
            val stage = currentStage ?: return@addEventHandler
            val eventStart = startEventPosition ?: return@addEventHandler
            val stageStart = startStagePosition ?: return@addEventHandler

            stage.x = stageStart.x + event.screenX - eventStart.x
            stage.y = stageStart.y + event.screenY - eventStart.y
        }

        component.addEventHandler(MouseEvent.MOUSE_RELEASED) {
            startEventPosition = null
            startStagePosition = null
        }
    }
}
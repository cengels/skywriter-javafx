package com.cengels.skywriter.fragments

import com.cengels.skywriter.style.ThemedStylesheet
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import tornadofx.*

class ThemedTitlebar(val viewTitle: String, val showMinimize: Boolean = false, val showMaximize: Boolean = true, val showClose: Boolean = true) : Fragment() {
    override val root = borderpane {
        addClass(ThemedStylesheet.titleBar)
        makeDraggable(this)

        left {
            stackpane {
                alignment = Pos.CENTER
                label(viewTitle)
            }
        }

        right {
            hbox {
                if (showMinimize) {
                    button("_").action { currentStage?.isIconified = true }
                }
                if (showMaximize) {
                    button("#").action { currentStage?.isMaximized = !currentStage!!.isMaximized }
                }
                if (showClose) {
                    button("X").action { close() }
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
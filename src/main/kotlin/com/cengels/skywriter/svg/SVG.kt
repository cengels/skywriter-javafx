package com.cengels.skywriter.svg

import com.cengels.skywriter.style.GeneralStylesheet
import com.cengels.skywriter.util.listen
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.layout.Region
import javafx.scene.shape.*
import tornadofx.addClass
import tornadofx.plusAssign
import tornadofx.getValue
import tornadofx.setValue
import kotlin.math.min

/** A group of SVG [Shape]s including the ability to scale and align the child shapes however desired. */
class SVG(width: Number? = null, height: Number? = null, scaleToFit: Boolean = true, maintainAspectRatio: Boolean = true, alignment: Pos = Pos.CENTER) : Region() {
    private val group = Group()
    val scaleToFitProperty = SimpleBooleanProperty(scaleToFit)
    /** If true, the contained nodes will be scaled to match the size of this element. Default is true. */
    var scaleToFit: Boolean by scaleToFitProperty
    val maintainAspectRatioProperty = SimpleBooleanProperty(maintainAspectRatio)
    /** If true, [scaleToFit] will always respect aspect ratio, sizing the elements contained within only to either height or width, whichever is smaller. Default is true. */
    var maintainAspectRatio: Boolean by maintainAspectRatioProperty
    val alignmentProperty = SimpleObjectProperty(alignment)
    /** Aligns the contained nodes to this element. If [scaleToFit] is true and [maintainAspectRatio] is false, this has no effect. Default is [Pos.CENTER]. */
    var alignment: Pos by alignmentProperty

    // This constructor is technically redundant but helpful for IDE parameter info.
    constructor(size: Number, scaleToFit: Boolean = true, maintainAspectRatio: Boolean = true, alignment: Pos = Pos.CENTER)
            : this(size, size, scaleToFit, maintainAspectRatio, alignment)

    init {
        addClass(GeneralStylesheet.svg)
        if (width != null) this.setSize(width, height ?: width)
        this += group

        listen(this.alignmentProperty, this.scaleToFitProperty, this.maintainAspectRatioProperty) {
            this.isNeedsLayout = true
        }
    }

    fun addPath(path: String): SVG {
        this.group += SVGPath().apply { content = path }

        return this
    }

    fun addLine(startX: Number, startY: Number, endX: Number, endY: Number): SVG {
        this.group += Line(startX.toDouble(), startY.toDouble(), endX.toDouble(), endY.toDouble())

        return this
    }

    fun addPolyline(vararg points: Number): SVG {
        this.group += Polyline(*points.map { it.toDouble() }.toDoubleArray())

        return this
    }

    fun addRectangle(width: Number, height: Number): SVG {
        this.group += Rectangle(width.toDouble(), height.toDouble())

        return this
    }

    fun addRectangle(x: Number, y: Number, width: Number, height: Number): SVG {
        this.group += Rectangle(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

        return this
    }

    /** Gets or sets the shapes in this [SVG] group. */
    val shapes: ObservableList<Shape> get() = this.group.children as ObservableList<Shape>

    protected override fun layoutChildren() {
        if (scaleToFit) {
            scaleToFit()
        }

        if (scaleToFit && !maintainAspectRatio) {
            group.relocate(0.0, 0.0)
        } else {
            group.relocate(this.computeX(), this.computeY())
        }
    }

    fun setSize(size: Number) {
        this.setSize(size, size)
    }

    fun setSize(width: Number, height: Number) {
        this.maxWidth = width.toDouble()
        this.minWidth = this.maxWidth
        this.prefWidth = this.maxWidth

        this.maxHeight = height.toDouble()
        this.minHeight = this.maxHeight
        this.prefHeight = this.maxHeight
    }

    private fun scaleToFit() {
        group.relocate(0.0, 0.0)
        val scaleX = this.width / group.layoutBounds.width
        val scaleY = this.height / group.layoutBounds.height

        if (maintainAspectRatio) {
            val min = min(scaleX, scaleY)
            group.scaleX = min
            group.scaleY = min
        } else {
            group.scaleX = scaleX
            group.scaleY = scaleY
        }
    }

    private fun computeX(): Double {
        return when (alignment) {
            Pos.TOP_LEFT,
            Pos.CENTER_LEFT,
            Pos.BASELINE_LEFT,
            Pos.BOTTOM_LEFT -> 0.0
            Pos.TOP_CENTER,
            Pos.CENTER,
            Pos.BASELINE_CENTER,
            Pos.BOTTOM_CENTER -> (this.width - group.layoutBounds.width) / 2
            Pos.TOP_RIGHT,
            Pos.CENTER_RIGHT,
            Pos.BASELINE_RIGHT,
            Pos.BOTTOM_RIGHT -> this.width - group.layoutBounds.width
            else -> 0.0
        }
    }

    private fun computeY(): Double {
        return when (alignment) {
            Pos.TOP_LEFT,
            Pos.TOP_CENTER,
            Pos.TOP_RIGHT -> 0.0
            Pos.CENTER_LEFT,
            Pos.CENTER,
            Pos.CENTER_RIGHT -> (this.height - group.layoutBounds.height) / 2
            Pos.BASELINE_LEFT,
            Pos.BASELINE_CENTER,
            Pos.BASELINE_RIGHT,
            Pos.BOTTOM_LEFT,
            Pos.BOTTOM_CENTER,
            Pos.BOTTOM_RIGHT -> this.height - group.layoutBounds.height
            else -> 0.0
        }
    }
}
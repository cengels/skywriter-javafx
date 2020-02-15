package com.cengels.skywriter.util

import com.cengels.skywriter.enum.ImageSizingType
import com.cengels.skywriter.util.convert.ColorConverter
import javafx.beans.binding.Binding
import javafx.beans.property.Property
import javafx.geometry.Insets
import javafx.geometry.Side
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.stage.FileChooser
import tornadofx.*
import java.awt.Color

/** Creates a binding that automatically converts the [java.awt.Color] values into [javafx.scene.paint.Paint] values. */
 fun Color.toBackground(): Background {
     return Background(BackgroundFill(ColorConverter.convert(this), CornerRadii.EMPTY, Insets.EMPTY))
 }

/** Creates a binding that automatically converts the [javafx.scene.paint.Color] values into [javafx.scene.paint.Paint] values. */
fun Property<javafx.scene.paint.Color>.backgroundBinding(): Binding<Background> {
    return this.objectBinding {
        if (it == null) {
            return@objectBinding Background(BackgroundFill(javafx.scene.paint.Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY))
        }
        return@objectBinding Background(BackgroundFill(it, CornerRadii.EMPTY, Insets.EMPTY))
    } as Binding<Background>
}

/** Inserts the specified number of paragraphs of lorem ipsum into the text field. */
fun Label.loremIpsum(paragraphs: Int = 3) {
    if (paragraphs > 0) {
        text += "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris iaculis vel ipsum id bibendum. Cras faucibus nibh vel massa ornare, at iaculis est vehicula. Cras et scelerisque ante, vitae porttitor risus. Integer eu elit non odio dapibus aliquam in vitae ligula. Nunc convallis, ipsum quis volutpat condimentum, velit massa tempus mi, quis fringilla ligula est pharetra diam. Nulla nec nisl vel mauris sagittis dignissim. Vivamus id varius ex. Pellentesque imperdiet sit amet nisl eget finibus. Aenean in felis sodales, faucibus nunc id, malesuada ante. Proin bibendum tellus massa, at semper erat scelerisque congue. Vivamus sollicitudin dolor sed erat hendrerit fermentum. Etiam ante nisi, ultricies nec porttitor at, volutpat sit amet risus. Maecenas luctus lacus eu lectus ultrices, non condimentum libero elementum. Vestibulum lacinia imperdiet lorem vitae porttitor."
    }

    if (paragraphs > 1) {
        text += "\nInteger fringilla dui ut tellus aliquet pharetra. Pellentesque pharetra, libero id lacinia consectetur, velit ipsum faucibus nisl, eget porta elit enim vitae nibh. Aenean lobortis nisi sed fringilla lobortis. Etiam volutpat ipsum velit, auctor ornare orci egestas mattis. Duis vehicula mi vel mauris fringilla, eu mollis ante sollicitudin. Praesent velit nisl, pellentesque et ante nec, elementum porttitor neque. Sed a felis efficitur, maximus nisl vel, egestas felis. Suspendisse vitae mi eget nulla porta mollis. Cras ac ipsum tempus, gravida risus nec, condimentum nisl. Aenean pharetra, elit at posuere sollicitudin, ex lectus convallis lorem, et pretium metus quam sed ex. Maecenas efficitur enim in turpis venenatis dapibus. Nunc at rutrum lacus. Vestibulum nibh felis, consequat eget nisl et, porta lacinia massa. Vivamus luctus sapien sit amet ligula iaculis, nec suscipit lectus iaculis. Morbi finibus viverra ante, nec viverra lorem ornare quis. Fusce ligula lectus, consectetur vel faucibus at, scelerisque vitae nibh."
    }

    if (paragraphs > 2) {
        text += "\nPellentesque sed magna et velit consectetur ultrices. Vivamus eleifend nulla et fermentum laoreet. Nam suscipit consequat convallis. Maecenas et tortor orci. Sed vulputate mattis convallis. Nullam sit amet nunc augue. Aliquam erat volutpat."
    }

    if (paragraphs > 3) {
        text += "\nMorbi ac lectus ut quam sollicitudin congue. Sed sed odio risus. Integer sit amet leo nec nibh tincidunt varius sit amet sollicitudin metus. Donec tincidunt felis augue, vel varius sem molestie in. Vivamus nec elementum magna. Praesent non est massa. Mauris suscipit odio nec turpis pulvinar viverra. Curabitur et elementum elit, et pharetra eros. Sed ac justo ornare, suscipit urna ac, luctus ligula. Sed ornare odio vestibulum, maximus dolor sit amet, ultricies velit. Nullam non nisi vestibulum, volutpat lectus eget, consectetur augue. Maecenas at lectus nec purus semper consequat. Phasellus eleifend eros risus, suscipit pulvinar sem dapibus eu. Quisque laoreet libero consequat aliquet blandit. Duis quis nulla at lectus cursus cursus eleifend sit amet ex."
    }

    if (paragraphs > 4) {
        text += "Quisque laoreet aliquet mattis. Nunc suscipit leo massa, a tincidunt est faucibus vitae. Nunc facilisis mi dui, eget vehicula urna malesuada eget. Duis non augue vitae metus iaculis commodo. Duis eu commodo mi. Integer pretium nisl risus, ut venenatis ante fringilla ac. Nulla ut nunc dui. Phasellus ac leo libero. Pellentesque nec malesuada metus, eget iaculis nunc. Nulla blandit sodales mollis. Donec aliquet hendrerit risus, non interdum ipsum. Ut at faucibus risus."
    }
}

val imageExtensionFilter: FileChooser.ExtensionFilter by lazy { FileChooser.ExtensionFilter("Images", "*.JPG", "*.BMP", "*.PNG", "*.GIF", "*.JPEG", "*.MPO") }

fun getBackgroundFor(color: javafx.scene.paint.Color, image: String? = null, imageSizingType: ImageSizingType = ImageSizingType.CONTAIN): Background {
    if (image.isNullOrBlank()) {
        return Background(BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY))
    }

    return Background(
        arrayOf(BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)),
        arrayOf(BackgroundImage(Image("file:///${image}"),
            if (imageSizingType == ImageSizingType.TILE) BackgroundRepeat.REPEAT else BackgroundRepeat.NO_REPEAT,
            if (imageSizingType == ImageSizingType.TILE) BackgroundRepeat.REPEAT else BackgroundRepeat.NO_REPEAT,
            if (imageSizingType != ImageSizingType.TILE) BackgroundPosition.CENTER else BackgroundPosition.DEFAULT,
            BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false,
                imageSizingType == ImageSizingType.CONTAIN,
                imageSizingType == ImageSizingType.COVER))))
}

fun getBackgroundFor(color: Color, image: String? = null, imageSizingType: ImageSizingType = ImageSizingType.CONTAIN): Background {
    return getBackgroundFor(ColorConverter.convert(color), image, imageSizingType)
}

/** Adds a change listener to the selected Property<T> and calls it immediately. */
fun <T> Property<T>.onChangeAndNow(op: (it: T?) -> Unit) {
    this.onChange(op)
    op(this.value)
}
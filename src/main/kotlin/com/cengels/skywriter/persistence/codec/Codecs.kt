package com.cengels.skywriter.persistence.codec

import com.cengels.skywriter.persistence.PlainTextCodec
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.Paragraph
import org.fxmisc.richtext.model.StyledSegment


interface DocumentCodec :
    PlainTextCodec<List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>, Any> {
    /** The DataFormat to apply the codecs on. If the DataFormat does not match, the codec is not used. */
    val dataFormat: DataFormat
}

interface ParagraphCodec<in T> :
    PlainTextCodec<Paragraph<MutableCollection<String>, String, MutableCollection<String>>, T>
interface SegmentCodec<in T> : PlainTextCodec<List<StyledSegment<String, MutableCollection<String>>>, T>
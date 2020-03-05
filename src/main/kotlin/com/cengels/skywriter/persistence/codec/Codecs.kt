package com.cengels.skywriter.persistence.codec

import com.cengels.skywriter.persistence.PlainTextCodec
import javafx.scene.input.DataFormat
import org.fxmisc.richtext.model.Paragraph
import org.fxmisc.richtext.model.StyledSegment


interface CodecGroup<in TDocument, in T> {
    val DOCUMENT_CODEC: DocumentCodec<TDocument>
    val PARAGRAPH_CODEC: ParagraphCodec<T>
    val SEGMENT_CODEC: SegmentCodec<T>
}

interface DocumentCodec<in T> :
    PlainTextCodec<List<Paragraph<MutableCollection<String>, String, MutableCollection<String>>>, T> {
    /** The DataFormat to apply the codecs on. If the DataFormat does not match, the codec is not used. */
    val dataFormat: DataFormat
}

interface ParagraphCodec<in T> :
    PlainTextCodec<Paragraph<MutableCollection<String>, String, MutableCollection<String>>, T>
interface SegmentCodec<in T> : PlainTextCodec<List<StyledSegment<String, MutableCollection<String>>>, T>

package com.cengels.skywriter.util

import org.fxmisc.richtext.model.*

typealias StyleClassedDocument = StyledDocument<MutableCollection<String>, String, MutableCollection<String>>
typealias ReadOnlyStyleClassedDocument = ReadOnlyStyledDocument<MutableCollection<String>, String, MutableCollection<String>>
typealias EditableStyleClassedDocument = EditableStyledDocument<MutableCollection<String>, String, MutableCollection<String>>
typealias StyleClassedParagraph = Paragraph<MutableCollection<String>, String, MutableCollection<String>>
typealias StyleClassedSegment = StyledSegment<String, MutableCollection<String>>
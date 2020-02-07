package com.cengels.skywriter

import com.cengels.skywriter.style.FormattingStylesheet
import com.cengels.skywriter.writer.WriterView
import tornadofx.App

class SkyWriterApp : App(WriterView::class, FormattingStylesheet::class)
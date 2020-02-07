package com.cengels.skywriter.writer

import tornadofx.ViewModel

class WriterViewModel : ViewModel() {
    val textDocument = TextDocument()

    val text = bind { textDocument.textProperty }
}
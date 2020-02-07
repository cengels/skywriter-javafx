package com.cengels.skywriter.persistence

import javafx.scene.text.TextAlignment
import org.fxmisc.richtext.model.Codec
import org.fxmisc.richtext.model.StyledDocument
import org.fxmisc.richtext.model.StyledSegment
import java.io.*


class MarkdownParser(val document: StyledDocument<MutableCollection<String>, String, MutableCollection<String>>) {
    companion object {
        var bufferedWriter: BufferedWriter? = null

        val DOCUMENT_CODEC = object: Codec<StyledDocument<MutableCollection<String>, String, MutableCollection<String>>> {
            override fun getName(): String = "segment-codec"

            override fun encode(os: DataOutputStream?, t: StyledDocument<MutableCollection<String>, String, MutableCollection<String>>) {
                t.paragraphs.forEach { paragraph ->
                    PARAGRAPH_CODEC.encode(os, paragraph.paragraphStyle)
                    paragraph.styledSegments.forEach { segment ->
                        SEGMENT_CODEC.encode(os, segment)
                    }
                }
            }

            override fun decode(`is`: DataInputStream): StyledDocument<MutableCollection<String>, String, MutableCollection<String>> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        val PARAGRAPH_CODEC = object: Codec<MutableCollection<String>> {
            override fun getName(): String = "paragraph-codec"

            override fun encode(os: DataOutputStream?, t: MutableCollection<String>) {
                println("paragraph $t")
            }

            override fun decode(`is`: DataInputStream): MutableCollection<String> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        val SEGMENT_CODEC = object: Codec<StyledSegment<String, MutableCollection<String>>> {
            override fun getName(): String = "segment-codec"

            override fun encode(os: DataOutputStream?, t: StyledSegment<String, MutableCollection<String>>) {
                val escapedText: String = t.segment.replace("*", "\\*").replace("_", "\\_").replace("#", "\\#")
                var text: String = escapedText
                if (t.style.contains("italic")) {
                    text = text.surround("*")
                }

                if (t.style.contains("bold")) {
                    text = text.surround("**")
                }

                bufferedWriter.apply {
                    println(text)
                    this!!.write(text)
                }
            }

            override fun decode(`is`: DataInputStream): StyledSegment<String, MutableCollection<String>> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }

    fun save(file: File) {
        try {
            val fileOutputStream = FileOutputStream(file)
            bufferedWriter = BufferedWriter(OutputStreamWriter(fileOutputStream, "UTF-8"))
            DOCUMENT_CODEC.encode(null, this.document)
            bufferedWriter!!.close()
            fileOutputStream.close()

            bufferedWriter = null
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }
}

fun String.surround(with: String): String = "$with$this$with"
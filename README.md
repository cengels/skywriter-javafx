# What is this?

This is a repository for Skywriter, a distraction-free writer that exports directly to Markdown, written in Kotlin and JavaFX.

## Legacy

This is a legacy project. You may be looking for [this repository](https://github.com/cengels/skywriter), which is a reimplementation of this project in Qt.

Do note that the linked repository is a non-functional work-in-progress. The project contained in this repository is, however, in a **fully working and functional state**, apart from some bugs and limitations caused by the underlying framework, so feel free to check it out and compile it if you wish. I still use this project exclusively for my own writing while development on the new Qt project is ongoing.

## Why was this project abandoned in favour of the new skywriter project?

It was a difficult decision to make. Kotlin is a fun language to write in and TornadoFX enhances the readability of JavaFX code by a significant margin. However, while coding the application, I still couldn't help but feel both the lack of robustness and solid documentation for JavaFX.

One big example is the lack of a dedicated and powerful rich-text component embedded in JavaFX. To counteract this, I used the open-source framework [RichTextFX](https://github.com/FXMisc/RichTextFX) at first. While RichTextFX is a great framework to create simple applications with rich-text processing capabilities, it proved to have too many limitations to be useful for Skywriter. To circumvent those limitations, I would have had to manipulate RichTextFX's own source code due to the closed nature of its code (i.e. most components are private and not extensible), which would have made upgrades to new versions difficult or even impossible.

Qt, on the other hand, has dedicated components for rich-text processing as well as clear and concise documentation for virtually everything it offers. I knew it would take a long time to port everything I've done so far into a new Qt project, but in the end I felt it was best to ensure that I could build the rich-text editor I envisioned.

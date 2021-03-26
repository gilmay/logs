package gma.logs.app.view

import tornadofx.*

class AppStyle : Stylesheet() {
    companion object {
        val thinBorder by cssclass()

        val checkboxTableColumn by cssclass()

        val tinyHoverButton by cssclass()

        val dropdownLikeButton by cssclass()
    }

    init {
        buttonBar {
            padding = box(.9.em)
        }

        thinBorder {
            padding = box(1.ex)
        }

        dropdownLikeButton {
            prefWidth = 2.3.em
            minWidth = 2.3.em
            maxWidth = 2.3.em
        }

        checkboxTableColumn {
            prefWidth = 2.em
            minWidth = 2.em
            maxWidth = 2.em
        }

        tinyHoverButton {
            prefWidth = 2.em
            minWidth = 2.em
            maxWidth = 2.em
            opacity = .7
            padding = box(0.px)
        }

    }

}

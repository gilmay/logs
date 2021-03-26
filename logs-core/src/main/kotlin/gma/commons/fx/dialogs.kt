package gma.commons.fx

import javafx.scene.control.TextInputDialog
import javafx.stage.Window

fun input(
    header: String,
    default: String = "",
    content: String = "",
    owner: Window? = null,
    title: String? = null
): String? =
    with(TextInputDialog(default)) {
        title?.let { this.title = it }
        this.headerText = header
        this.contentText = content
        owner?.also { this.initOwner(it) }

        return this.showAndWait().orElse(null)
    }

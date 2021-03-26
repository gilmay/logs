package gma.commons.fx

import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import tornadofx.*

fun ButtonBar.okButton(text: String = "OK", op: Button.() -> Unit = {}) =
    this.button(text, ButtonBar.ButtonData.OK_DONE, null) {
        isDefaultButton = true
        op()
    }

fun ButtonBar.cancelButton(text: String = "Cancel", op: Button.() -> Unit = {}) =
    this.button(text, ButtonBar.ButtonData.CANCEL_CLOSE, null) {
        isCancelButton = true
        op()
    }

fun ButtonBar.applyButton(text: String = "Apply", op: Button.() -> Unit = {}) =
    this.button(text, ButtonBar.ButtonData.APPLY, null, op)

fun ButtonBar.yesButton(text: String = "Yes", op: Button.() -> Unit = {}) =
    this.button(text, ButtonBar.ButtonData.YES, null, op)

fun ButtonBar.noButton(text: String = "No", op: Button.() -> Unit = {}) =
    this.button(text, ButtonBar.ButtonData.NO, null, op)

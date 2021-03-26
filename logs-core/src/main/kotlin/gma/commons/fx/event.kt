package gma.commons.fx

import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

/** Returns true if this is a [MouseButton.PRIMARY] double click */
val MouseEvent.isActionClick get() = clickCount == 2 && button == MouseButton.PRIMARY

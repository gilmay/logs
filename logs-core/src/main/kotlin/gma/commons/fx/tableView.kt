package  gma.commons.fx

import javafx.beans.binding.BooleanBinding
import javafx.beans.property.Property
import javafx.scene.control.TableView
import tornadofx.*

val <T> TableView<T>.selectedItemProperty: Property<T>
    get() = selectionModelProperty().select { it.selectedItemProperty() }

val <T> TableView<T>.anyItemSelectedBinding: BooleanBinding
    get() = selectedItemProperty.booleanBinding { it != null }

val <T> TableView<T>.isAnyItemSelected: Boolean
    get() = anyItemSelectedBinding.value

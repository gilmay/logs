package gma.logs.app.model.config.spi

import javafx.beans.property.StringProperty

interface BindingFactory {
    fun bind(property: StringProperty): StringProperty
}

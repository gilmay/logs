package gma.logs.app.controller.bookmark

import gma.logs.app.model.bookmark.BookmarkManager
import gma.logs.app.model.bookmark.LogsBookmark
import gma.logs.app.model.config.ConfigurationCategory
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import tornadofx.*
import java.util.Comparator.comparing
import java.util.TreeMap

class BookmarksController : Controller() {

    val selectedItemProperty = SimpleObjectProperty<Any>()

    private val bookmarksByConfigurationCategory =
        TreeMap<ConfigurationCategory, List<LogsBookmark>>(comparing(ConfigurationCategory::name))

    val treeFormatter: TreeCell<*>.(Any) -> Unit = {
        text = if (it is LogsBookmark) {
            it.name
        } else {
            (it as ConfigurationCategory).name
        }
    }

    fun reload() {
        bookmarksByConfigurationCategory.clear()

        bookmarksByConfigurationCategory += BookmarkManager.bookmarksByName.values
            .groupBy { it.configuration.category }
    }

    fun treeChildFactory(item: TreeItem<Any>): Iterable<Any>? = with(item.value) {
        when (this) {
            is ConfigurationCategory -> bookmarksByConfigurationCategory[this]
            is LogsBookmark -> null
            else /* root */ -> bookmarksByConfigurationCategory.keys
        }
    }

    val canDeleteSelectedBookmarkBinding = selectedItemProperty.booleanBinding { it is LogsBookmark }
    fun onDeleteSelectedBookmark(): Boolean {
        (selectedItemProperty.value as? LogsBookmark)?.let {
            confirm("Delete bookmark: ${it.name}") {
                BookmarkManager.delete(it)
                reload()
                return true
            }
        }
        return false
    }

    val canOpenBinding = selectedItemProperty.booleanBinding { it is LogsBookmark }
    fun onOpen(): Boolean {
        fire(OpenBookmarkEvent(selectedItemProperty.value as LogsBookmark))
        return true
    }

}

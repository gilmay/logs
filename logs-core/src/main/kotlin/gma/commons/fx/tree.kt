package gma.commons.fx

import javafx.beans.property.Property
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import tornadofx.*

fun <T> TreeView<T>.bindSelectedBidirectional(property: Property<T>) {
    bindSelected(property)
    property.onChange { item ->
        if (item != null) {
            findExpandAndSelect(item)
        } else {
            selectionModel.clearSelection()
        }
    }
}

fun <T> TreeView<T>.findExpandAndSelect(select: T) = matchExpandAndSelect { it == select }

fun <T> TreeView<T>.matchExpandAndSelect(predicate: (T?) -> Boolean) =
    findAndExpand(predicate, root)?.run {
        selectionModel.clearAndSelect(this)
        scrollTo(this)
        getTreeItem(this)
    }

private fun <T> TreeView<T>.findAndExpand(
    predicate: (T?) -> Boolean,
    item: TreeItem<T>?
): Int? {
    if (item != null) {
        if (predicate(item.value)) {
            return getRow(item)
        }
        val expanded = item.isExpanded
        item.isExpanded = true
        item.children.forEach {
            val row = findAndExpand(predicate, it)
            if (row != null) {
                return row
            }
        }
        item.isExpanded = expanded
    }
    return null
}

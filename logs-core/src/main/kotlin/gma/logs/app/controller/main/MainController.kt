package gma.logs.app.controller.main

import gma.commons.fx.TabsItemViewModel.Companion.tabsItemViewModel
import gma.commons.fx.input
import gma.commons.fx.subscribeWithExceptionHandler
import gma.logs.app.controller.bookmark.OpenBookmarkEvent
import gma.logs.app.controller.logsinstance.LogsScope
import gma.logs.app.controller.logsinstance.LogsTabViewModel
import gma.logs.app.controller.openconfig.OpenConfigurationEvent
import gma.logs.app.model.bookmark.BookmarkManager
import gma.logs.app.view.logsinstance.LogsTabFragment
import javafx.beans.binding.BooleanBinding
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import tornadofx.*

class MainController : Controller() {

    val tabsItemViewModel = tabsItemViewModel<LogsScope, LogsTabFragment>()

    init {
        subscribeWithExceptionHandler<OpenConfigurationEvent> {
            tabsItemViewModel.itemList.add(LogsScope(it.configuration))
        }
        subscribeWithExceptionHandler<OpenBookmarkEvent> {
            LogsScope.fromBookmark(it.bookmark).let { scope ->
                tabsItemViewModel.itemList.add(scope)
                scope.refresh()
            }
        }
        tabsItemViewModel.onClosed = { (it as LogsScope).dispose() }
    }

    val canSaveBookmarkBinding: BooleanBinding = tabsItemViewModel.itemProperty.isNotNull
    fun onSaveBookmark(): Boolean =
        tabsItemViewModel.itemProperty.value?.let { scope ->
            tabsItemViewModel.tabPane?.selectionModel?.selectedItem
            val controller = find<LogsTabViewModel>(scope)
            if (controller.isDirty) {
                alert(
                    Alert.AlertType.WARNING,
                    "Save Bookmark",
                    "Current modifications are not applied",
                    ButtonType.OK,
                    ButtonType.CANCEL
                ) {
                    if (it != ButtonType.OK) return@let false
                }
            }

            input("Save Bookmark", scope.bookmarkName ?: "", title = "Save Bookmark")?.let { name ->
                BookmarkManager.save(scope.toBookmark(name))
                scope.bookmarkName = name
            }
            return@let true
        } ?: false

}

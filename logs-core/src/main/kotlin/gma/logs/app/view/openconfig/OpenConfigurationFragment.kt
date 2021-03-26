package gma.logs.app.view.openconfig

import gma.logs.app.controller.openconfig.OpenConfigurationsController
import gma.logs.app.model.bookmark.BookmarkManager
import gma.logs.app.view.AppStyle
import gma.logs.app.view.bookmark.OpenBookmarkTreeFragment
import gma.logs.app.view.config.ConfigurationsFragment
import javafx.scene.control.TabPane
import tornadofx.*

class OpenConfigurationFragment : Fragment("Open") {

    companion object {
        fun openModal() = FX.find<OpenConfigurationFragment>(Scope()).openModal()
    }

    private val controller by inject<OpenConfigurationsController>()

    override fun onDock() {
        if (controller.configurationsByCategory.values.flatten().isEmpty()
            && BookmarkManager.bookmarksByName.isEmpty()
        ) {
            ConfigurationsFragment.openModal()
        }
    }

    override fun onUndock() = scope.deregister()

    override val root = tabpane {
        addClass(AppStyle.thinBorder)

        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab(OpenConfigurationTreeFragment::class)
        tab(OpenBookmarkTreeFragment::class)
    }

}

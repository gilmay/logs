package gma.logs.app.view.main

import gma.logs.app.controller.main.MainController
import gma.logs.app.view.AppStyle
import gma.logs.app.view.openconfig.OpenConfigurationFragment
import gma.logs.app.view.system.StatusBarFragment
import tornadofx.*

class MainView : View("Logs") {

    val controller by inject<MainController>()

    override val root = borderpane {
        addClass(AppStyle.thinBorder)
        setPrefSize(800.0, 600.0)

        top = toolbar(
            button("\uD83D\uDDC1") {
                action { OpenConfigurationFragment.openModal() }
            },
            button("\uD83D\uDCBE") {
                enableWhen(controller.canSaveBookmarkBinding)
                action { controller.onSaveBookmark() }
            }
        )

        center = tabpane {
            controller.tabsItemViewModel.tabPane = this
        }

        bottom = find<StatusBarFragment>().root
    }

}

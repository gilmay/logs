package gma.logs.app.view.openconfig

import gma.commons.fx.cancelButton
import gma.commons.fx.isActionClick
import gma.commons.fx.okButton
import gma.logs.app.controller.openconfig.OpenConfigurationsController
import gma.logs.app.view.AppStyle
import gma.logs.app.view.config.ConfigurationsFragment
import javafx.scene.control.TreeItem
import tornadofx.*

class OpenConfigurationTreeFragment : Fragment("Configurations") {

    private val controller by inject<OpenConfigurationsController>()

    override fun onUndock() = scope.deregister()

    override val root = borderpane {
        addClass(AppStyle.thinBorder)

        top = toolbar(
            button("Edit configurations...") {
                action {
                    ConfigurationsFragment.openModal()
                }
            }
        )

        center = treeview<Any>(TreeItem()) {
            isShowRoot = false
            cellFormat(formatter = controller.treeFormatter)
            bindSelected(controller.selectedItemProperty)
            populate(childFactory = controller::treeChildFactory)
            root.expandTo(2)
            controller.configurationsByCategoryProperty.onChange {
                populate(childFactory = controller::treeChildFactory)
                root.expandTo(2)
            }
            setOnMouseClicked {
                if (it.isActionClick && controller.canOpenBinding.value && controller.onOpen()) {
                    currentStage!!.close()
                }
            }
        }

        bottom = buttonbar {
            okButton("Open") {
                enableWhen { controller.canOpenBinding }
                action {
                    if (controller.onOpen()) {
                        currentStage!!.close()
                    }
                }
            }
            cancelButton {
                action {
                    currentStage!!.close()
                }
            }
        }
    }
}

package gma.logs.app.view.config

import gma.commons.fx.bindSelectedBidirectional
import gma.logs.app.controller.config.ConfigurationsEditController
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import tornadofx.*

class ConfigurationsTreeFragment : Fragment() {

    private val controller by inject<ConfigurationsEditController>()

    private var tree by singleAssign<TreeView<Any>>()

    override val root = borderpane {
        top = toolbar(
            button("➕") {
                action {
                    controller.onAddNewConfiguration(tree)
                }
                enableWhen(controller.canAddNewConfigurationBinding)
            },
            button("\uD83D\uDFAC") {
                action {
                    controller.onDeleteSelectedConfiguration()
                }
                enableWhen(controller.canDeleteSelectedConfigurationBinding)
            },
            button("❐") {
                action {
                    controller.onDuplicateSelectedConfiguration(tree)
                }
                enableWhen(controller.canDuplicateSelectedConfigurationBinding)
            }
        )

        center = treeview(TreeItem<Any>()) {
            tree = this
            isShowRoot = false
            cellFormat(formatter = controller.treeFormatter)
            populate(childFactory = controller::treeChildFactory)
            root.expandTo(2)
            bindSelectedBidirectional(controller.selectedItemProperty)
            selectFirst()
        }
    }

}

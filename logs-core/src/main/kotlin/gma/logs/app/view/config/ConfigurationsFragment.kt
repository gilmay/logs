package gma.logs.app.view.config

import gma.commons.fx.applyButton
import gma.commons.fx.cancelButton
import gma.commons.fx.okButton
import gma.logs.app.controller.config.ConfigurationScope
import gma.logs.app.controller.config.ConfigurationsEditController
import gma.logs.app.view.AppStyle
import javafx.geometry.Insets
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import tornadofx.*

class ConfigurationsFragment : Fragment("Configurations") {

    companion object {
        fun openModal() =
            find<ConfigurationsFragment>(Scope()).openModal()
    }

    override fun onUndock() = scope.deregister()

    private val controller by inject<ConfigurationsEditController>()

    override val root = object : BorderPane() {
        override fun computePrefWidth(height: Double): Double {
            return (left as Region).prefWidth(USE_COMPUTED_SIZE) * 3.5
        }
    }.apply {
        addClass(AppStyle.thinBorder)

        left = find<ConfigurationsTreeFragment>().root

        bottom = buttonbar {
            okButton {
                action {
                    if (controller.okAction()) {
                        currentStage!!.close()
                    }
                }
            }
            cancelButton {
                action {
                    currentStage!!.close()
                }
            }
            applyButton {
                action {
                    controller.okAction()
                }
            }
        }

    }

    private val configurationFragments =
        HashMap<ConfigurationScope, ConfigurationEditCommonTreeFragment>().also { map ->
            controller.selectedItemProperty.onChange {
                root.center = (it as? ConfigurationScope)?.let { scope ->
                    if (scope in map) {
                        map[scope]
                    } else {
                        val fragment =
                            find<ConfigurationEditCommonTreeFragment>(scope)
                        fragment.root.center = ScrollPane(scope.fragment.root).apply {
                            isFitToWidth = true
                        }
                        map[scope] = fragment
                        fragment
                    }
                }?.root
            }
        }

}

class ConfigurationEditCommonTreeFragment : Fragment() {

    override val scope = super.scope as ConfigurationScope

    override val root = borderpane {
        top = form {
            fieldset {
                padding = Insets.EMPTY
                field("Name") {
                    textfield(scope.nameBinding).required()
                }
            }
        }

        bottom = buttonbar {
            button("Revert") {
                enableWhen { scope.itemViewModel.dirty }
                action { scope.itemViewModel.rollback() }
            }
        }
    }

}

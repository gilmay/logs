package gma.logs.app.view.bookmark

import gma.commons.fx.bindSelectedBidirectional
import gma.commons.fx.cancelButton
import gma.commons.fx.isActionClick
import gma.commons.fx.okButton
import gma.logs.app.controller.bookmark.BookmarksController
import gma.logs.app.view.AppStyle
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import tornadofx.*

class OpenBookmarkTreeFragment : Fragment("Bookmarks") {

    private val controller by inject<BookmarksController>()

    private var tree by singleAssign<TreeView<Any>>()

    init {
        controller.reload()
    }

    override val root = borderpane {
        addClass(AppStyle.thinBorder)

        top = toolbar(
            button("\uD83D\uDFAC") {
                action {
                    if (controller.onDeleteSelectedBookmark()) {
                        with(center as TreeView<Any>) {
                            populate(childFactory = controller::treeChildFactory)
                            root.expandTo(2)
                        }
                    }
                }
                enableWhen(controller.canDeleteSelectedBookmarkBinding)
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

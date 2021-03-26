package gma.commons.fx

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class TabsItemViewModel<T : Any, U : UIComponent>(
    val itemClass: KClass<T>,
    val itemList: ObservableList<T> = observableListOf(),
    val tabComponentClass: KClass<out U>,
    val tabComponentScopeProvider: (T) -> Scope = { FX.defaultScope }
) : ItemViewModel<T>() {

    companion object {
        inline fun <reified T : Any, reified U : UIComponent> tabsItemViewModel(
            itemList: ObservableList<T> = observableListOf(),
            noinline tabComponentScopeProvider: (T) -> Scope = { FX.defaultScope }
        ) = TabsItemViewModel(T::class, itemList, U::class, tabComponentScopeProvider)

        inline fun <reified T : Scope, reified U : UIComponent> tabsItemViewModel(
            itemList: ObservableList<T> = observableListOf()
        ) = TabsItemViewModel(T::class, itemList, U::class) { it }
    }

    var onClosed: ((Scope) -> Unit)? = null

    private val tabPaneSelectionModelListener = ChangeListener<Tab?> { _, _, new ->
        item = (itemClass.safeCast(new?.userData))
    }

    val tabPaneProperty = SimpleObjectProperty<TabPane>().apply {
        addListener(ChangeListener { _, old, new ->
            itemList.forEach { old?.closeTabWithUserData(it) }
            old?.selectionModel?.selectedItemProperty()?.removeListener(tabPaneSelectionModelListener)

            new?.apply {
                itemList.forEach {
                    this.addTab(this@TabsItemViewModel, it)
                }
                selectionModel.selectedItemProperty().addListener(tabPaneSelectionModelListener)
            }
        })
    }
    var tabPane: TabPane? by tabPaneProperty

    init {
        itemProperty.onChange { tabPane?.findTabOrNull(it)?.select() }
    }

    var selectLastOpen = true

    var deregisterOnClose = true

    val itemListChangeListener = ListChangeListener<T> { c ->
        while (c.next()) {
            c.removed.forEach {
                tabPane?.closeTabWithUserData(it)
            }
            c.addedSubList.forEach {
                val tab = tabPane?.addTab(this, it)
                if (selectLastOpen) {
                    tabPane?.selectionModel?.select(tab)
                    // Workaround for the first initial tab:
                    itemProperty.value = itemClass.safeCast(tabPane?.selectionModel?.selectedItem?.userData)
                }
            }
        }
    }.also { itemList.addListener(it) }

}

private fun <T : Any, U : UIComponent> TabPane.addTab(model: TabsItemViewModel<T, U>, item: T): Tab {
    val scope = model.tabComponentScopeProvider(item)
    return tab(find(model.tabComponentClass, scope)) {
        userData = item
        onCloseRequest = EventHandler { evt ->
            if ((evt.source as Tab).userData == item) {
                model.itemList.remove(item)
                evt.consume()
            }
        }
        onClosed = EventHandler {
            if (model.deregisterOnClose) {
                scope.deregister()
            }
            model.onClosed?.let { it(scope) }
        }
    }
}

private fun <T> TabPane.closeTabWithUserData(item: T) = tabs.firstOrNull { it.userData == item }?.apply {
    close()
    // Workaround onClosed not being called automatically when removed from parent
    onClosed?.handle(Event(Tab.CLOSED_EVENT))
}

private fun <T> TabPane.findTabOrNull(item: T) = tabs.firstOrNull { it.userData == item }

package gma.commons.fx

import tornadofx.*

inline fun <reified T : FXEvent> Component.subscribeWithExceptionHandler(
    times: Number? = null,
    noinline action: EventContext.(T) -> Unit
): EventRegistration {
    return subscribe<T>(times) {
        try {
            action(it)
        } catch (ex: Throwable) {
            Thread.currentThread().uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), ex)
            throw ex
        }
    }
}

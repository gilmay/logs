package gma.logs.app

import gma.logs.app.view.AppStyle
import gma.logs.app.view.main.MainView
import javafx.scene.image.Image
import javafx.stage.Stage
import tornadofx.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class LogsApp : App(MainView::class, AppStyle::class) {

    override fun start(stage: Stage) {
        stage.icons += Image("gma/logs/app/logs.png")
        super.start(stage)
    }

    override fun stop() {
        super.stop()
        thread(isDaemon = true) {
            /*
             * In case some unclosed LogRepositories have kept some non daemon background threads running
             */
            Thread.sleep(5_000)
            exitProcess(0)
        }
    }
}

fun main(args: Array<String>) {
    launch<LogsApp>(args)
}

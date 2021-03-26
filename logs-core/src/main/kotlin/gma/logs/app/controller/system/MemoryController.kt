package gma.logs.app.controller.system

import tornadofx.*
import java.lang.management.ManagementFactory
import java.lang.management.MemoryUsage
import java.text.NumberFormat
import java.util.Timer
import kotlin.concurrent.timerTask

class MemoryController : Controller() {

    private val memoryMx = ManagementFactory.getMemoryMXBean()

    init {
        Timer(true)
            .scheduleAtFixedRate(timerTask { update() }, 0, 5.seconds.toMillis().toLong())
    }

    private fun update() =
        runLater {
            heapNonHeapProperty.value = (memoryMx.heapMemoryUsage to memoryMx.nonHeapMemoryUsage)
        }

    fun gc() = runAsync {
        memoryMx.gc()
        update()
    }

    private val heapNonHeapProperty = objectProperty<Pair<MemoryUsage, MemoryUsage>>()

    val heapUsagePercentBinding = heapNonHeapProperty.doubleBinding {
        it?.let { it.first.used.toDouble() / it.first.max } ?: 0.0
    }

    val heapUsageTextBinding = heapNonHeapProperty.stringBinding {
        val nf = NumberFormat.getIntegerInstance()
        it?.let { "${nf.format(it.first.used.toMega())} / ${nf.format(it.first.max.toMega())} M" } ?: ""
    }

    val memoryTextBinding = heapNonHeapProperty.stringBinding {
        it?.let {
            val nf = NumberFormat.getIntegerInstance()
            """Heap: ${it.first.toUserString(nf)}
                |Non Heap: ${it.second.toUserString(nf)}
            """.trimMargin("|")
        } ?: ""
    }

    private fun MemoryUsage.toUserString(nf: NumberFormat) =
        nf.format(used.toMega()) +
                if (max > 0) {
                    " / ${nf.format(max.toMega())} M"
                } else " M"

    private fun Long.toMega() = toDouble() / 1024 / 1024

}

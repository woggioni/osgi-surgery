package net.woggioni.osgi.surgery.bundles.a

import net.woggioni.osgi.surgery.bundles.b.B
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class Activator : BundleActivator {
    companion object {
        private val log = LoggerFactory.getLogger(BundleActivator::class.java)
        private var l = mutableListOf<String>()
    }

    private var workerRunning = AtomicBoolean(false)

    val worker = Runnable {
        var counter = 0
        while(workerRunning.get()) {
            log.info("Running ${counter}, squared: ${B.square(counter)}")
            counter++
            Thread.sleep(1000)
        }
    }

    private var thread : Thread? = null

    override fun start(context: BundleContext?) {
        l.add("Started")
        log.debug(l.toString())
        workerRunning.set(true)
        thread = Thread(worker).apply {
            start()
        }
    }

    override fun stop(context: BundleContext?) {
        l.add("Stopped")
        if(System.getProperty("thread.leak")?.toBoolean() != true) {
            workerRunning.set(false)
            thread?.join()
        }
    }
}

class A {}
package net.woggioni.osgi.surgery.bundles.a

import net.woggioni.osgi.surgery.bundles.b.B
import net.woggioni.osgi.surgery.bundles.b.BComponent
import org.osgi.annotation.bundle.Header
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.osgi.service.component.ComponentContext
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "\${@class}")
class Activator : BundleActivator {
    companion object {
        private val log = LoggerFactory.getLogger(Activator::class.java)
        private var l = mutableListOf<String>()
    }

    private var workerRunning = AtomicBoolean(false)

    private var thread : Thread? = null

    override fun start(context: BundleContext) {
        log.info("Started ${context.bundle.symbolicName}")
        l.add("Started")
        log.debug(l.toString())
        workerRunning.set(true)
        thread = Thread {
            var counter = 0
            while(workerRunning.get()) {
                log.info("Running ${counter}, squared: ${B.square(counter)}")
                counter++
                Thread.sleep(1000)
            }
        }.apply {
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

@Component
class AComponent @Activate constructor(@Reference bComponent: BComponent) {

    private companion object {
        val log = LoggerFactory.getLogger(AComponent::class.java)
    }

    fun activate(componentContext : ComponentContext) {
        log.info("Activating ${AComponent::class.qualifiedName}")
    }

    init {
        log.info("Constructing ${AComponent::class.qualifiedName}")
    }
}


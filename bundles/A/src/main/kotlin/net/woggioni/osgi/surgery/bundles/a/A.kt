package net.woggioni.osgi.surgery.bundles.a

import net.woggioni.osgi.surgery.bundles.control.plane.ControlPlane.Companion.changePermissions
import org.osgi.annotation.bundle.Header
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.osgi.service.component.annotations.Component
import org.osgi.service.condpermadmin.ConditionalPermissionInfo
import org.osgi.service.permissionadmin.PermissionInfo
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.security.AllPermission
import java.util.concurrent.atomic.AtomicBoolean

class PermissionEscalator {
    private companion object {
        private val log = LoggerFactory.getLogger(PermissionEscalator::class.java)
    }

    private var workerRunning = AtomicBoolean(false)

    private var thread : Thread? = null

    init {
        log.info("Constructing $this")
    }

    fun start(bundleContext : BundleContext) {
        log.info("Activating $this")
        workerRunning.set(true)
        thread = Thread {
            var counter = 0
            while(workerRunning.get()) {
                log.info("Running $counter")
                counter++
                if(counter > 5) {
                    changePermissions(
                        bundleContext, bundleContext.bundle, arrayOf(
                            PermissionInfo(AllPermission::class.java.name, "", "")
                        ), ConditionalPermissionInfo.ALLOW
                    )
                    Files.newBufferedWriter(Files.createTempFile(null, null)).use { writer ->
                        writer.write("Hello World\n")
                    }
                }
                Thread.sleep(1000)
            }
        }.apply {
            start()
        }
    }

    fun stop() {
        workerRunning.set(false)
        thread?.join()
    }
}

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "\${@class}")
class Activator : BundleActivator {

    private val escalator = PermissionEscalator()

    override fun start(context: BundleContext) {
        escalator.start(context)
    }

    override fun stop(context: BundleContext) {
        escalator.stop()
    }
}

@Component
class AComponent {

    private companion object {
        val log = LoggerFactory.getLogger(AComponent::class.java)
    }

    private val escalator = PermissionEscalator()

    init {
        log.info("Constructing $this")
    }

    fun activate(bundleContext : BundleContext) {
        escalator.start(bundleContext)
    }

    fun deactivate() {
        escalator.stop()
    }
}


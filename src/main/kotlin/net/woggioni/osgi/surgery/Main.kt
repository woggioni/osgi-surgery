package net.woggioni.osgi.surgery

import org.osgi.framework.*
import org.osgi.framework.launch.Framework
import org.osgi.framework.launch.FrameworkFactory
import org.slf4j.LoggerFactory
import java.io.*
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

enum class BundleState(val code: Int, val description : String) {
    STARTING(Bundle.STARTING, "starting"),
    INSTALLED(Bundle.INSTALLED, "installed"),
    STOPPING(Bundle.STOPPING, "stopping"),
    ACTIVE(Bundle.ACTIVE, "active"),
    RESOLVED(Bundle.RESOLVED, "resolved"),
    UNINSTALLED(Bundle.UNINSTALLED, "uninstalled");

    object Builder {
        fun fromCode(code: Int) = values().find { it.code == code } ?: throw IllegalArgumentException("Unknown bundle state with code $code")
    }
}

class Container : Closeable {
    companion object {
        private const val FELIX_FRAMEWORK_FACTORY_FQN = "org.apache.felix.framework.FrameworkFactory"

        private val log = LoggerFactory.getLogger(Container::class.java)

        val frameWorkFactory: FrameworkFactory
            get() = Class.forName(FELIX_FRAMEWORK_FACTORY_FQN)
                .getDeclaredConstructor().newInstance() as FrameworkFactory

        private const val SYSTEM_PACKAGES_FILE = "META-INF/system_packages"
        private fun loadSystemPackages() = Container::class.java.classLoader
                .getResource(SYSTEM_PACKAGES_FILE)?.let { resourceUrl ->
            resourceUrl.openStream().bufferedReader().useLines { lines ->
                lines.map { line -> line.substringBefore('#') }
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .joinToString(",")
            }
        } ?: throw IOException("'$SYSTEM_PACKAGES_FILE' not found")
    }

    private val storageDir = Files.createTempDirectory("osgi-surgery")

    private val framework : Framework

    init {
        val propertyMap = (sequenceOf(
            Constants.FRAMEWORK_STORAGE to storageDir.toString(),
            Constants.FRAMEWORK_STORAGE_CLEAN to Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT,
            Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA to loadSystemPackages(),
        ) + System.getProperties().asSequence().map { it.key as String to it.value as String }).toMap()
        framework = frameWorkFactory.newFramework(propertyMap)
    }

    fun start() {
        log.info("Starting OSGi framework ${framework::class.java.canonicalName} ${framework.version}")
        framework.start()
        framework.bundleContext.addBundleListener { evt ->
            val bundle = evt.bundle
            log.debug("Bundle ${bundle.location} ID = ${bundle.bundleId} ${bundle.symbolicName ?: ""} ${bundle.version} ${BundleState.Builder.fromCode(bundle.state).description}")
        }
        log.info("OSGi framework ${framework::class.java.canonicalName} ${framework.version} started")
    }

    fun installBundle(bundlePath : Path) : Long {
        val bundleContext = framework.bundleContext ?: throw IllegalStateException("OSGi framework not active yet.")
        return BufferedInputStream(Files.newInputStream(bundlePath)).use { inputStream ->
            bundleContext.installBundle(bundlePath.toUri().toString(), inputStream).bundleId
        }
    }

    fun install(bundlePath : Path) {
        val bundleContext = framework.bundleContext ?: throw IllegalStateException("OSGi framework not active yet.")
        when {
            Files.isDirectory(bundlePath) -> {
                log.debug("Loading bundles from folder '${bundlePath}'")
                Files.list(bundlePath).filter(Files::isRegularFile).forEach(::install)
            }
            Files.isRegularFile(bundlePath) && bundlePath.fileName.toString().endsWith(".jar") -> {
                log.debug("Installing $bundlePath")
                BufferedInputStream(Files.newInputStream(bundlePath)).use { inputStream ->
                    bundleContext.installBundle(bundlePath.toUri().toString(), inputStream)
                }
            }
            else -> {
                log.trace("Ignoring $bundlePath")
            }
        }
    }

    fun activate(vararg bundleId : Long) {
        val ctx = framework.bundleContext
        when {
            bundleId.isEmpty() -> ctx.bundles.asSequence().filter { bundle ->
                bundle.headers.get(Constants.FRAGMENT_HOST) == null &&
                    (bundle.state == BundleState.INSTALLED.code || bundle.state == BundleState.RESOLVED.code)
            }
            else -> bundleId.asSequence().map (ctx::getBundle)
        }.forEach { bundle ->
            bundle.start()
        }
    }

    fun stop(vararg bundleId : Long) {
        val ctx = framework.bundleContext
        when {
            bundleId.isEmpty() -> ctx.bundles.asSequence().filter { bundle ->
                bundle.headers.get(Constants.FRAGMENT_HOST) != null &&
                bundle.state == BundleState.ACTIVE.code
            }
            else -> bundleId.asSequence().map (ctx::getBundle)
        }.forEach { bundle ->
            bundle.stop()
        }
    }

    fun uninstall(vararg bundleId : Long) {
        val ctx = framework.bundleContext
        when {
            bundleId.isEmpty() -> ctx.bundles.asSequence().filter { bundle ->
                bundle.headers.get(Constants.FRAGMENT_HOST) != null &&
                        (bundle.state == BundleState.INSTALLED.code || bundle.state == BundleState.RESOLVED.code)
            }
            else -> bundleId.asSequence().map (ctx::getBundle)
        }.forEach { bundle ->
            bundle.uninstall()
        }
    }


    fun <T> withContext(action : (BundleContext) -> T) : T = action(framework.bundleContext)

    override fun close() {
        framework.stop()
        while(true) {
            val exit = framework.waitForStop(5000L).let { evt ->
                when (evt.type) {
                    FrameworkEvent.ERROR -> {
                        log.error(evt.throwable.message, evt.throwable)
                        throw evt.throwable
                    }
                    FrameworkEvent.WAIT_TIMEDOUT -> {
                        log.warn("OSGi framework shutdown timed out")
                        false
                    }
                    FrameworkEvent.STOPPED -> {
                        true
                    }
                    else -> {
                        throw NotImplementedError("Unknown event type ${evt.type}")
                    }
                }
            }
            if(exit) break
        }
        Files.walk(storageDir)
            .sorted(Comparator.reverseOrder())
            .forEach(Files::delete)
    }
}

object Launcher {

    @JvmStatic
    fun main(vararg arg : String) {
        Container().use { cnt ->
            cnt.run {
                start()
                arg.forEach {
                    Paths.get(it).let(::install)
                }
                activate()
                while(true) Thread.sleep(10000)
            }
        }
    }
}
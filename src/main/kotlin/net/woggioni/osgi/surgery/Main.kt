package net.woggioni.osgi.surgery

import org.osgi.framework.Constants
import org.osgi.framework.FrameworkEvent
import org.osgi.framework.launch.Framework
import org.osgi.framework.launch.FrameworkFactory
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.nio.file.Files

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

    override fun close() {
        framework.waitForStop(10000L).let { evt ->
            when (evt.type) {
                FrameworkEvent.ERROR -> {
                    log.error(evt.throwable.message, evt.throwable)
                    throw evt.throwable
                }
                FrameworkEvent.WAIT_TIMEDOUT -> {
                    log.warn("OSGi framework shutdown timed out")
                }
                else -> {}
            }

        }
        Files.walk(storageDir)
            .sorted(Comparator.reverseOrder())
            .forEach(Files::delete)
    }
}

object Launcher {
    @JvmStatic
    fun main(vararg arg : String) {
        Container(arg).use {

        }
    }
}
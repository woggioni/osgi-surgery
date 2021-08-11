package net.woggioni.osgi.surgery.bundles.b

import org.osgi.annotation.bundle.Header
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.slf4j.LoggerFactory

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "\${@class}")
class Activator : BundleActivator {
    companion object {
        private val log = LoggerFactory.getLogger(Activator::class.java)
    }

    override fun start(context: BundleContext) {
        log.info("Started ${context.bundle.symbolicName}")
    }

    override fun stop(context: BundleContext) {

    }
}
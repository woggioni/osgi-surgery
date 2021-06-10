package bundles.a

import bundles.b.Child
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

class Bar : Child()

class Activator : BundleActivator {

    override fun start(context: BundleContext) {
        val bar = Bar()
        bar::class.members
    }

    override fun stop(context: BundleContext) {}
}


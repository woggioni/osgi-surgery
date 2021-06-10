package net.woggioni.osgi.surgery.bundles.a

import net.woggioni.osgi.surgery.bundles.b.KotlinChild
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext

class Bar : KotlinChild {
    override fun foo() {
        TODO("Not yet implemented")
    }

    override fun bar() {
        TODO("Not yet implemented")
    }
}

class Activator : BundleActivator {

    override fun start(context: BundleContext?) {
        val bar = Bar()
        println(bar::class.members)
    }

    override fun stop(context: BundleContext?) {}
}


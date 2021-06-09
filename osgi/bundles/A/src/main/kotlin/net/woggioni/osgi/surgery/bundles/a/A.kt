package net.woggioni.osgi.surgery.bundles.a

import net.woggioni.osgi.surgery.bundles.b.KotlinChild
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.slf4j.LoggerFactory
import kotlin.reflect.full.memberProperties

class Bar : KotlinChild {
    override fun foo() {
        TODO("Not yet implemented")
    }

    override fun bar() {
        TODO("Not yet implemented")
    }
}

class Activator : BundleActivator {
    companion object {
        private val log = LoggerFactory.getLogger(BundleActivator::class.java)
    }

    override fun start(context: BundleContext?) {
        val bar = Bar()
        println(bar::class.memberProperties)
    }

    override fun stop(context: BundleContext?) {}
}


package net.woggioni.odgi.surgery

import net.woggioni.osgi.surgery.BundleState
import net.woggioni.osgi.surgery.Container
import org.junit.jupiter.api.Test
import org.osgi.framework.Bundle
import java.lang.RuntimeException
import java.nio.file.Paths

class SurgeryTest {

    @Test
    fun test() {
        Container().use { cnt ->
            cnt.run {
                start()
                val bundleAPath = Paths.get(System.getProperty("bundles.A"))
                val bundleBPath = Paths.get(System.getProperty("bundles.B"))
                val bundleB2Path = Paths.get(System.getProperty("bundles.B2"))

                var idA = installBundle(bundleAPath)
                val idB = installBundle(bundleBPath)
                activate(idA, idB)
                for(i in 1..5) Thread.sleep(1000)
                stop(idA)
                Thread.sleep(2000)
                activate(idA)
                Thread.sleep(3000)
                stop(idA)
                Thread.sleep(1000)
                uninstall(idA)
                System.getProperty("bundles.A").let {
                    install(Paths.get(it))
                }
                activate()
                idA = withContext { ctx ->
                    ctx.bundles.asSequence()
                        .filter { it.state == BundleState.ACTIVE.code && it.symbolicName == "A"}
                        .map(Bundle::getBundleId)
                        .toList()
                        .toLongArray()
                }.first()
                activate(idA)
                Thread.sleep(3000)
                stop(idB)
                Thread.sleep(3000)
                uninstall(idB)
                Thread.sleep(3000)
                val idB2 = installBundle(bundleB2Path)
                activate(idB2)
                stop(idA)
                uninstall(idA)
                idA = installBundle(bundleAPath)
                activate(idA)
                Thread.sleep(3000)
                stop(idA)
            }
        }
    }
}
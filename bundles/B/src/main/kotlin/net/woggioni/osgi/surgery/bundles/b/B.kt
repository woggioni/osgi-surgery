package net.woggioni.osgi.surgery.bundles.b

import org.osgi.service.component.annotations.Component
import org.slf4j.LoggerFactory


class B {
    companion object {
        @JvmStatic
        fun square(n : Int) : Int = n * n
    }
}

@Component
class BComponent {
    private companion object {
        val log = LoggerFactory.getLogger(BComponent::class.java)
    }

    init {
        log.info("Constructing ${BComponent::class.qualifiedName}")
    }
}
package net.woggioni.osgi.surgery.bundles.b


class B {
    companion object {
        @JvmStatic
        fun square(n : Int) : Int = throw RuntimeException("KA-BOOM!!!")
    }
}
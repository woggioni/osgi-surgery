package net.woggioni.osgi.surgery.boo

import net.woggioni.osgi.surgery.bundles.b.KotlinChild

class Bar : KotlinChild {
    override fun foo() {
        TODO("Not yet implemented")
    }

    override fun bar() {
        TODO("Not yet implemented")
    }
}

fun main(vararg args : String) {
    println(Bar::class.supertypes)
}
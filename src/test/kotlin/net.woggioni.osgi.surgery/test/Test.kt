package net.woggioni.osgi.surgery.test

import bundles.b.Child
import org.junit.jupiter.api.Test
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

class Bar : Child() {
    fun foo() : Child? = null

    var field = 5
}

class Test {

    @Test
    fun `KClass∶∶members`() {
        Bar::class.members
    }

    @Test
    fun `KClass∶∶declaredMembers`() {
        Bar::class.declaredMembers
    }

    @Test
    fun `KClass∶∶memberProperties`() {
        Bar::class.memberProperties
    }

    @Test
    fun `KClass∶∶declaredMemberProperties`() {
        Bar::class.declaredMemberProperties
    }

    @Test
    fun `KClass∶∶functions`() {
        Bar::class.functions
    }

    @Test
    fun `KClass∶∶declaredFunctions`() {
        Bar::class.declaredFunctions
    }

    @Test
    fun `KClass∶∶memberExtensionProperties`() {
        Bar::class.memberExtensionProperties
    }

    @Test
    fun `KClass∶∶declaredMemberExtensionProperties`() {
        Bar::class.declaredMemberExtensionProperties
    }

    @Test
    fun `KClass∶∶isAbstract`() {
        Bar::class.isAbstract
    }

    @Test
    fun `KClass∶∶isCompanion`() {
        Bar::class.isCompanion
    }

    @Test
    fun `KClass∶∶isData`() {
        Bar::class.isData
    }

    @Test
    fun `KClass∶∶isFinal`() {
        Bar::class.isFinal
    }

    @Test
    fun `KClass∶∶isFun`() {
        Bar::class.isFun
    }

    @Test
    fun `KClass∶∶isInner`() {
        Bar::class.isInner
    }
    @Test
    fun `KClass∶∶isOpen`() {
        Bar::class.isOpen
    }

    @Test
    fun `KClass∶∶supertypes`() {
        Bar::class.supertypes
    }

    @Test
    fun `KClass∶∶superclasses`() {
        Bar::class.superclasses
    }

    @Test
    fun `KClass∶∶sealedSubclasses`() {
        Bar::class.sealedSubclasses
    }

    @Test
    fun `KClass∶∶constructors`() {
        Bar::class.constructors
    }

    @Test
    fun `KClass∶∶allSupertypes`() {
        Bar::class.allSupertypes
    }

    @Test
    fun `KClass∶∶allSuperclasses`() {
        Bar::class.allSuperclasses
    }

    @Test
    fun `KClass∶∶visibility`() {
        Bar::class.visibility
    }

    @Test
    fun `KClass∶∶jvmName`() {
        Bar::class.jvmName
    }

    @Test
    fun `KType∶∶jvmName`() {
        Bar::class.jvmName
    }

    @Test
    fun `Method∶∶kotlinFunction`() {
        Bar::class.java.getMethod("foo").kotlinFunction
    }

    @Test
    fun `Method∶∶kotlinProperty`() {
        Bar::class.java.getDeclaredField("field").kotlinProperty
    }

    @Test
    fun `KProperty∶∶javaMethod`() {
        Bar::foo.javaMethod
    }

    @Test
    fun `KProperty∶∶javaField`() {
        Bar::field.javaField
    }

    @Test
    fun `KProperty∶∶javaGetter`() {
        Bar::field.javaGetter
    }

    @Test
    fun `KProperty∶∶javaSetter`() {
        Bar::field.javaSetter
    }

    @Test
    fun `KClass∶∶primaryConstructor`() {
        Bar::class.primaryConstructor
    }

    @Test
    fun `KFunction∶∶javaConstructor`() {
        Bar::class.primaryConstructor!!.javaConstructor
    }

    @Test
    fun `KType∶∶javaType`() {
        Bar::class.starProjectedType.javaType
    }

}
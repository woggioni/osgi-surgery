import java.io.InputStream
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

interface KotlinParent {
    fun bar()
}

interface KotlinChild : KotlinParent {
    fun foo()
}

class Bar : KotlinChild {
    override fun foo() {
        TODO("Not yet implemented")
    }

    override fun bar() {
        TODO("Not yet implemented")
    }
}

class CostrainedClassLoader(val classNames: Set<String>, parent: ClassLoader?) : ClassLoader(parent) {

    constructor(parent: ClassLoader?, vararg classes: Class<*>)
            : this(classes.map(Class<*>::getName).toSet(), parent)

    constructor(parent: ClassLoader?, vararg classes: KClass<*>)
            : this(classes.map(KClass<*>::java).map(Class<*>::getName).toSet(), parent)

    private var ongoingClassDefinition = 0

    fun canBeDelegated(className : String) = className.startsWith("java.") || className.startsWith("kotlin.")

    override fun findClass(className: String): Class<*> {
        return className.takeIf(classNames::contains)
            ?.let {
                getSystemClassLoader().getResource(it.replace('.', '/') + ".class")
            }?.openStream()?.use(InputStream::readAllBytes)
            ?.let { buffer ->
                ++ongoingClassDefinition
                defineClass(className, buffer, 0, buffer.size).also {
                    --ongoingClassDefinition
                }
            } ?: let{
            throw ClassNotFoundException(className)
        }
    }

    override fun loadClass(name: String, resolve : Boolean): Class<*> {
        return (findLoadedClass(name) ?: when {
            ongoingClassDefinition > 0 || canBeDelegated(name) -> super.loadClass(name, resolve)
            name in classNames -> findClass(name)
            else -> throw ClassNotFoundException(name)
        }).also {
            if(resolve) resolveClass(it)
        }
    }
}

class Runner : Runnable {
    override fun run() {
        println(Bar::class.memberProperties)
    }
}

fun main(vararg args : String) {
    val jars = System.getProperty("java.class.path").split(System.getProperty("path.separator"))
        .asSequence()
        .filter { it.endsWith(".jar") }
        .filter { jarPath ->
            jarPath.substring(jarPath.lastIndexOf('/').takeIf {it != -1} ?: 0).contains("kotlin")
        }.map { Path.of(it) }
        .filter(Files::exists)
        .filterNot(Files::isDirectory)
        .map(Path::toUri)
        .map(URI::toURL)
        .toList()
        .toTypedArray()

    val baseClassLoader = URLClassLoader(jars, null)
    val cl1 = CostrainedClassLoader(baseClassLoader, KotlinParent::class)
    val cl2 = CostrainedClassLoader(cl1, KotlinChild::class)
    val cl3 = CostrainedClassLoader(cl2, Bar::class, Runner::class)
    val runnerClass = cl3.loadClass(Runner::class.java.name) as Class<Runnable>
    val runner = runnerClass.getConstructor().newInstance()
    runner.run()
} 
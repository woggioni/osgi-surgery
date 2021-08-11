package net.woggioni.osgi.surgery.bundles.control.plane

import org.osgi.annotation.bundle.Header
import org.osgi.framework.AdminPermission
import org.osgi.framework.Bundle
import org.osgi.framework.BundleActivator
import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.osgi.framework.ServicePermission
import org.osgi.service.condpermadmin.BundleLocationCondition
import org.osgi.service.condpermadmin.ConditionInfo
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin
import org.osgi.service.condpermadmin.ConditionalPermissionInfo
import org.osgi.service.permissionadmin.PermissionInfo
import org.slf4j.LoggerFactory
import java.io.FilePermission
import java.lang.IllegalStateException
import java.security.AllPermission
import java.util.PropertyPermission

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "\${@class}")
class ControlPlane : BundleActivator {

    companion object {
        private val log = LoggerFactory.getLogger(ControlPlane::class.java)

        fun changePermissions(
            ctx: BundleContext,
            bundle: Bundle,
            permissionInfos: Array<PermissionInfo>,
            what: String
        ) {
            val ref = ctx.getServiceReference(ConditionalPermissionAdmin::class.java)
                ?: throw IllegalStateException("No provider was found for service '${ConditionalPermissionAdmin::class.java.name}'")
            try {
                val cpa = ctx.getService(ref)
                log.info(
                    "Changing permissions on bundle {name: {}, version: {}, id: {}}",
                    bundle.symbolicName,
                    bundle.version,
                    bundle.bundleId
                )
                val cpu = cpa.newConditionalPermissionUpdate()
                val cpi = cpa.newConditionalPermissionInfo(
                    null, arrayOf(
                        ConditionInfo(
                            BundleLocationCondition::class.java.name, arrayOf(bundle.location)
                        )
                    ), permissionInfos, what
                )
                cpu.conditionalPermissionInfos.add(0, cpi)
                cpu.commit()
            } finally {
                ctx.ungetService(ref)
            }
        }
    }

    override fun start(context: BundleContext) {
        changePermissions(
            context, context.bundle, arrayOf(
                PermissionInfo(AllPermission::class.java.name, "", "")
            ), ConditionalPermissionInfo.ALLOW
        )
        context.bundles.filter {
            it.symbolicName in setOf("org.apache.felix.scr", "org.osgi.util.promise", "org.osgi.util.function")
        }.forEach {
            changePermissions(
                context, it, arrayOf(
                    PermissionInfo(AdminPermission::class.java.name, "*", "listener,metadata"),
                    PermissionInfo(PropertyPermission::class.java.name, "*", "read,write"),
                    PermissionInfo(ServicePermission::class.java.name, "*", "get,register"),
                ), ConditionalPermissionInfo.ALLOW
            )
        }

        val bundle = context.bundles.asSequence()
            .find { "A" == it.symbolicName } ?: throw RuntimeException("No bundle named 'A' was found")
        changePermissions(
            context, bundle, arrayOf(
                PermissionInfo(ServicePermission::class.java.name, "*", "get,register"),
            ), ConditionalPermissionInfo.ALLOW
        )
        changePermissions(
            context, bundle, arrayOf(
                PermissionInfo(FilePermission::class.java.name, "<<ALL FILES>>", "read,write,execute,delete"),
                ), ConditionalPermissionInfo.DENY
        )
    }

    override fun stop(context: BundleContext) {}
}
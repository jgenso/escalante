/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import io.escalante.logging.Log
import java.io.File
import org.jboss.vfs.{VFS, VirtualFile}
import org.jboss.as.server.deployment.module._
import io.escalante.SCALA_292
import io.escalante.modules.{JBossModule, JBossModulesRepository}
import io.escalante.maven.{MavenDependencyResolver, MavenArtifact}
import org.jboss.msc.service.ServiceRegistry
import io.escalante.lift.LIFT_24

/**
 * A deployment processor that hooks the right dependencies for the Lift
 * version desired.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftDependencyProcessor extends DeploymentUnitProcessor {

  import LiftDependencyProcessor._

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit
    info("Try to deploy: %s", deployment)
    val liftMetaData = deployment.getAttachment[LiftMetaData](LiftMetaData.ATTACHMENT_KEY)
    info("Metadata is: %s", liftMetaData)
    if (liftMetaData == null)
      return

    val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)

    val service = getLiftService(ctx.getServiceRegistry)
    val repo = new JBossModulesRepository(new File(service.thirdPartyModulesPath))

    // Attach shared dependencies depending of the lift version
    liftMetaData match {
      case LiftMetaData(LIFT_24, scala) =>
        moduleSpec.addSystemDependency(JODA_TIME_MODULE_ID.moduleDependency)
        moduleSpec.addSystemDependency(SLF4J_MODULE_ID.moduleDependency)
        moduleSpec.addSystemDependency(COMMONS_CODEC_MODULE_ID.moduleDependency)
        // Not shipped by JBoss AS, so download and install
        val module = repo.installModule(
          new MavenArtifact("commons-fileupload", "commons-fileupload", "1.2.2"),
          new JBossModule("javax.servlet.api"))
        moduleSpec.addSystemDependency(module.moduleDependency)
      case _ => // TODO: Throw exception
        info("Unknown Lift deployment")
    }

    // Attach Scala dependencies according to the Scala version passed
    liftMetaData match {
      case LiftMetaData(lift, SCALA_292) =>
        moduleSpec.addSystemDependency(SCALA_MODULE_ID.moduleDependency)
      case LiftMetaData(lift, scala) =>
        val module = repo.installScalaModule(scala)
        moduleSpec.addSystemDependency(module.moduleDependency)
      case _ => // TODO: Throw exception
        info("Unknown lift metadata")
        null
    }

    // Hack Lift jars into the war file cos Lift framework is currently
    // designed to be solely used by one web-app in (class loader) isolation
    liftMetaData match {
      case LiftMetaData(lift, scala) =>
        val liftJars = MavenDependencyResolver.resolveArtifact(
          liftMetaData.mavenArtifact, Some(liftMetaData.liftDependencyFilter))
        addLiftJars(deployment, liftJars)
      case _ => info("Unknown Lift deployment")
    }
  }

  def undeploy(unit: DeploymentUnit) {
    // TODO: Do I need to remove the dependencies?
  }

  private def addLiftJars(deployment: DeploymentUnit, jars: Seq[File]) {
    val resourceRoot = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
    val root = resourceRoot.getRoot

    jars.foreach {
      jar =>
        val temp = root.getChild("WEB-INF/lift-lib") // Virtual Lift mount point
        val repackagedJar = createZipRoot(temp, jar)
        ModuleRootMarker.mark(repackagedJar)
        deployment.addToAttachmentList(Attachments.RESOURCE_ROOTS, repackagedJar)
    }
  }

  private def createZipRoot(deploymentTemp: VirtualFile, file: File): ResourceRoot = {
    val archive = deploymentTemp.getChild(file.getName)
    val closable = VFS.mountZip(file, archive, TempFileProviderService.provider())
    new ResourceRoot(file.getName, archive, new MountHandle(closable))
  }

  private def getLiftService(registry: ServiceRegistry): LiftService = {
    val container = registry.getService(LiftService.createServiceName)
    container.getValue.asInstanceOf[LiftService]
  }

}

object LiftDependencyProcessor extends Log {

  val SCALA_MODULE_ID = new JBossModule("org.scala-lang.scala-library")

  val JODA_TIME_MODULE_ID = new JBossModule("org.joda.time")

  val SLF4J_MODULE_ID = new JBossModule("org.slf4j")

  val COMMONS_CODEC_MODULE_ID = new JBossModule("org.apache.commons.codec")

}
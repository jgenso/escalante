/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.maven

import io.escalante.modules.JBossModule
import org.sonatype.aether.graph.DependencyFilter

/**
 * Metadata representation of a Maven artifact.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class MavenArtifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val isMain: Boolean,
    val filter: Option[DependencyFilter]) {

  def this(groupId: String, artifactId: String, version: String) =
    this(groupId, artifactId, version, true, None)

  def this(groupId: String, artifactId: String, version: String, filter: DependencyFilter) =
    this(groupId, artifactId, version, true, Some(filter))

  private val moduleFriendlyArtifactId = artifactId.replace('.', '_')

  private val moduleFriendlyGroupId = groupId.replace('.', '/')

  private val moduleName: String = new java.lang.StringBuilder()
    .append(groupId).append('.')
    .append(moduleFriendlyArtifactId).toString

  private val slot: String = if (isMain) "main" else version

  /**
   * Full directory path compatible with JBoss Modules constructed
   * out of the metadata of this Maven artifact.
   */
  val moduleDirName: String = new java.lang.StringBuilder()
    .append(moduleFriendlyGroupId).append('/')
    .append(moduleFriendlyArtifactId).append('/')
    .append(slot).toString

  /**
   * Version-less jar file name compatible with JBoss Modules constructed
   * out of the metadata of this Maven artifact.
   */
  val moduleJarName: String = artifactId + ".jar"

  /**
   * Maven artifact metadata represented as JBossModule instance, which
   * represents metadata of a JBoss Module.
   */
  def jbossModule(export: Boolean): JBossModule =
    new JBossModule(moduleName, export, slot)

  /**
   * Returns the Maven artifact's coordinates.
   */
  def coordinates: String =
    new java.lang.StringBuilder()
      .append(groupId).append(":")
      .append(artifactId).append(":")
      .append(version)
      .toString

}

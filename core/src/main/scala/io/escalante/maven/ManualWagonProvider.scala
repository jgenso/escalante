/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.maven

import org.sonatype.aether.connector.wagon.WagonProvider
import org.apache.maven.wagon.Wagon
import org.apache.maven.wagon.providers.http.LightweightHttpWagon

/**
 * A wagon provided that uses no IoC container.

 * @author Galder Zamarreño
 * @since // TODO
 */
object ManualWagonProvider extends WagonProvider {

  def lookup(roleHint: String): Wagon =
    if (roleHint == "http") new LightweightHttpWagon() else null

  def release(wagon: Wagon) {
    // No-op
  }

}

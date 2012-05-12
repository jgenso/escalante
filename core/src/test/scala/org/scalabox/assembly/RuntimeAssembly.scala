package org.scalabox.assembly

import java.io.File
import org.scalabox.logging.Log
import org.scalabox.SCALA_291
import org.scalabox.modules.{JBossModule, JBossModulesRepository}

/**
 * // TODO: Is this really needed? Why not just call LiftModule.build?
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
object RuntimeAssembly extends Log {

   def build(destDir: File, module: ScalaBoxModule) {
      info("Build Lift extension and copy to container")

      module.build(destDir)

      // TODO: Pass this in the module build
      val repo = new JBossModulesRepository(destDir)

      repo.installModule(SCALA_291.maven, new JBossModule("javax.api"))
   }

}
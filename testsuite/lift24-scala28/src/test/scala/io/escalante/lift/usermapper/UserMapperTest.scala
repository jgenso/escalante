/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.usermapper

import model.User
import io.escalante.lift.AbstractLiftWebAppTest
import scala.xml.Elem
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.openqa.selenium.By
import collection.JavaConversions._

/**
 * Test for a Lift ORM model based application.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class UserMapperTest {

  val driver = new HtmlUnitDriver()

  @Test def testStaticContent() {
    driver.get("%s/static/index".format(appUrl))
    val source = driver.getPageSource
    assert(source.contains("Static content... everything you put in the /static"),
      "Instead, page source contains: " + source)
  }

  @Test def testUserSignUpAndLogin() {
    // Load sign up page
    driver.get("%s/user_mgt/sign_up".format(appUrl))
    // Fill in fields
    driver.findElement(By.id("txtFirstName")).sendKeys("Galder")
    driver.findElement(By.id("txtLastName")).sendKeys("Zamarreño")
    driver.findElement(By.id("txtEmail")).sendKeys("athletic@bilbao.com")
    asScalaIterable(driver.findElements(
      By.cssSelector("input[type='password']"))).foreach {
      elem =>
        elem.clear() // Clear the '*' from the field first
        elem.sendKeys("boomoo")
    }
    // Click on sign up
    driver.findElement(By.cssSelector("input[type='submit']")).click();

    // If sign up worked, 'Logout' should be found
    findLogout()

    // Click logout
    driver.get("%s/user_mgt/logout".format(appUrl))

    // Click on login and fill in details
    driver.get("%s/user_mgt/login".format(appUrl))
    driver.findElement(By.name("username")).sendKeys("athletic@bilbao.com")
    driver.findElement(By.name("password")).sendKeys("boomoo")
    driver.findElement(By.cssSelector("input[type='submit']")).click();

    // If sign up worked, 'Logout' should be found
    findLogout()
  }

  protected def appUrl: String = "http://localhost:8080/usermapper-282"

  private def findLogout() {
    val source = driver.getPageSource
    assert(source.contains("Logout"), "Instead, page source contains: " + source)
  }

}

object UserMapperTest extends AbstractLiftWebAppTest {

  @Deployment def deployment: WebArchive =
    deployment(Some("2.4"), Some("2.8.2"), classOf[UserMapperBoot])

  def deployment(lift: Option[String], scala: Option[String],
    bootClass: Class[_ <: AnyRef]): WebArchive =
    deployment("usermapper", "usermapper-%s.war".format(scala.get.replace(".", "")),
      lift, scala, bootClass, "io.escalante.lift.usermapper.UserMapperBoot",
      classOf[User], classOf[UserMapperTest])

  override val indexHtml: Elem =
    <html>
      <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
        <title>Home</title>
      </head>
      <body class="lift:content_id=main">
        <div id="main" class="lift:surround?with=default;at=content">
          <h2>Welcome to Escalante!</h2>
          <p>
            The home of Scala apps :)
          </p>
        </div>
      </body>
    </html>

  override val templates: Seq[String] = List(
    "templates-hidden/default.html", "templates-hidden/wizard-all.html")

  override val static: Option[Elem] = Some {
    <div id="main" class="lift:surround?with=default;at=content">
      Static content... everything you put in the /static
      directory will be served without additions to SiteMap
    </div>
  }

}

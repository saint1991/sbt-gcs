package com.github.saint1991.sbt.gcs

import java.security.KeyPairGenerator

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.runtime.{ universe => ru }

import com.google.auth.oauth2.ServiceAccountCredentials
import monix.reactive.Observer
import org.scalatest.matchers._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.PrivateMethodTester

class GcsTaskFactoryTest extends AnyWordSpec with should.Matchers with PrivateMethodTester {

  type TaskFactory = GcsTaskFactory[GcsTransfer]

  private val mirror = ru.runtimeMirror(getClass.getClassLoader)
  private val clz = ru.typeOf[TaskFactory].typeSymbol.asClass
  private val cm = mirror.reflectClass(clz)
  private val sym = ru.typeOf[TaskFactory].decl(ru.termNames.CONSTRUCTOR).asMethod
  private val constructor = cm.reflectConstructor(sym)
  private val configSym = ru.typeOf[TaskFactory].decl(ru.TermName("config")).asTerm

  private def newTaskFactory(): TaskFactory = constructor().asInstanceOf[TaskFactory]
  private def getConfig(f: TaskFactory): GcsTaskConfig = {
    val im = mirror.reflect(f)
    val fm = im.reflectField(configSym)
    fm.get.asInstanceOf[GcsTaskConfig]
  }

  private lazy val credential = {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(1024)
    ServiceAccountCredentials
      .newBuilder()
      .setClientId("saint1991")
      .setClientEmail("saint1991@email.com")
      .setPrivateKey(keyGen.genKeyPair().getPrivate)
      .setServiceAccountUser("saint1991")
      .setProjectId("test-project")
      .build()
  }
  private lazy val observer = Observer.stopped[Array[Byte]]

  "GcsTaskFactory" should {
    "construct GcsTaskConfig by withXXX methods" in {
      val factory = newTaskFactory()
      getConfig(factory) should equal(GcsTaskConfig())

      val c1 = factory.withCredential(credential)
      getConfig(c1) should equal(GcsTaskConfig(credential = Some(credential)))

      val c2 = c1.withChunkSize(1024)
      getConfig(c2) should equal(GcsTaskConfig(credential = Some(credential), chunkSize = 1024))

      val c3 = c2.withTimeout(1 second)
      getConfig(c3) should equal(GcsTaskConfig(credential = Some(credential), chunkSize = 1024, timeout = 1 second))

      val c4 = c3.withObservers(observers = Seq(observer))
      getConfig(c4) should equal(
        GcsTaskConfig(credential = Some(credential), chunkSize = 1024, timeout = 1 second, observers = Seq(observer))
      )
    }
  }
}

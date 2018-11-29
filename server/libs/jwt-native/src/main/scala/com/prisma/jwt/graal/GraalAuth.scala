package com.prisma.jwt.graal

import java.nio.charset.Charset

import com.prisma.jwt.Algorithm.Algorithm
import com.prisma.jwt.{Auth, AuthFailure, JwtGrant}
import org.graalvm.nativeimage.c.`type`.{CCharPointer, CTypeConversion}
import org.graalvm.word.WordFactory
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Try

case class GraalAuth(algorithm: Algorithm) extends Auth {
  def toJavaString(str: CCharPointer) = CTypeConversion.toJavaString(str)
  def toCString(str: String)          = CTypeConversion.toCString(str).get()

  println(new Hack)

  // expirationOffset is the offset in seconds to the current timestamp. None is no expiration at all (todo: edge case: -1).
  def createToken(secret: String, expirationOffset: Option[Long], grant: Option[JwtGrant]): Try[String] = Try {
    // Has to be this verbose to fight scala type inference / erasure and native image compiler.
    val target: CCharPointer = if (grant.isDefined) {
      val g: JwtGrant = grant.get
      val t: String   = g.target

      toCString(t)
    } else {
      WordFactory.nullPointer[CCharPointer]()
    }

    val action: CCharPointer = if (grant.isDefined) {
      val g: JwtGrant      = grant.get
      val a: String        = g.action
      val aa: CCharPointer = toCString(a)

      aa
    } else {
      WordFactory.nullPointer[CCharPointer]()
    }

//    val (action, target): (CCharPointer, CCharPointer) = grant match {
//      case Some(g) =>
//        (toCString(g.action), toCString(g.target))
//
//      case None =>
//        val np = WordFactory.nullPointer[CCharPointer]()
//        (np, np)
//    }

    val buffer: CIntegration.ProtocolBuffer = GraalRustBridge.create_token(
      toCString(algorithm.toString),
      toCString(secret),
      expirationOffset
        .map { e =>
          DateTime.now(DateTimeZone.UTC).plusSeconds(e.toInt).getMillis / 1000
        }
        .getOrElse(NO_EXP),
      target,
      action
    )

//    debug(buffer)
    throwOnError(buffer)

    if (buffer.getDataLen == 0) {
      throw AuthFailure("Native call returned no token")
    }

    val dataString = new String(CTypeConversion.asByteBuffer(buffer.getData, buffer.getDataLen.toInt).array(), Charset.forName("UTF-8"))

    GraalRustBridge.destroy_buffer(buffer)
    dataString
  }

  override def verifyToken(token: String, secrets: Vector[String], expectedGrant: Option[JwtGrant]): Try[Unit] = Try {
    createToken("asd", Some(1000000), None)
  }

  private def throwOnError(buffer: CIntegration.ProtocolBuffer): Unit = {
//    if (buffer.getError.isNonNull) {
//      val errorString = toJavaString(buffer.getError)
//      GraalRustBridge.destroy_buffer(buffer)
//
//      throw AuthFailure(errorString.capitalize)
//    }
  }

  private def debug(buffer: CIntegration.ProtocolBuffer): Unit = {
//    println(s"[Graal][Debug] ${buffer.toString}")
  }
}

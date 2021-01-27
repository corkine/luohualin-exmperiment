package com.mazhangjing.utils

import java.io.{PrintWriter, StringWriter}

import org.slf4j.{Logger, LoggerFactory}

object Logging {
  def record(e:Throwable): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    e.printStackTrace(pw)
    val res = sw.toString
    pw.close()
    sw.close()
    res
  }
}

trait Logging {
  protected val log: Logger = LoggerFactory.getLogger(getClass)
}
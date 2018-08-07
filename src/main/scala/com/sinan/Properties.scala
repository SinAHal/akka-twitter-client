package com.sinan

import com.typesafe.config.ConfigFactory

object Properties {
  val conf = ConfigFactory.load()

  // TODO: Make this generic
  def readString(path: String): String ={
    conf.getString(path)
  }
}

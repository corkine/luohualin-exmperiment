package com.mazhangjing.lab.screens

import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

import com.mazhangjing.lab.LabUtils.{goNextScreenSafe, ifKeyButton}
import com.mazhangjing.lab.{Experiment, Screen, ScreenAdaptor}
import javafx.concurrent.Task
import javafx.event.Event
import javafx.geometry.Pos
import javafx.scene.{Cursor, Scene}
import javafx.scene.input.KeyCode
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.{Font, Text, TextAlignment}

class RestScreen(val minRestTimeSeconds: Int) extends ScreenAdaptor {

  var showScreenTime: Instant = _

  val info = "请休息，休息结束后，请按空格键继续实验。"

  val text = new Text()

  val infoText = new Text()

  var isDisappearing = false

  override def callWhenShowScreen(): Unit = {
    showScreenTime = Instant.now()
  }

  override def initScreen(): Screen = {
    val r = new VBox()
    r.setAlignment(Pos.CENTER)
    text.setText(info)
    text.setTextAlignment(TextAlignment.CENTER)
    text.setFont(Font.font("STKaiti",40))
    infoText.setTextAlignment(TextAlignment.CENTER)
    infoText.setFont(Font.font("STKaiti",30))
    infoText.setFill(Color.DARKGRAY)
    r.setSpacing(20)
    r.getChildren.addAll(text, infoText)
    layout = r
    duration = 10000000
    information = if (minRestTimeSeconds == 0) "可控休息屏幕" else "强制最短休息屏幕"
    layout.setCursor(Cursor.NONE)
    this
  }

  override def eventHandler(event: Event, experiment: Experiment, scene: Scene): Unit = {
    ifKeyButton(KeyCode.SPACE, event) {
      if (minRestTimeSeconds != 0) {
        //如果是强制最短休息屏幕，并且现在时间已经超过规定时间，则允许
        val timeDiff = Duration.between(showScreenTime, Instant.now()).abs().getSeconds
        if (timeDiff > minRestTimeSeconds) {
          goNextScreenSafe
        } else {
          infoText.setText("最少还有 " + (minRestTimeSeconds - timeDiff) + " 秒")
          infoText.setVisible(true)
          if (!isDisappearing) {
            //让刺激消失
            isDisappearing = true
            val task = new Task[String]() {
              override def call(): String = {
                TimeUnit.SECONDS.sleep(3)
                infoText.setVisible(false)
                isDisappearing = false
                ""
              }
            }
            new Thread(task).start()
          }
        }
      } else {
        //如果没有强制休息，则继续
        goNextScreenSafe
      }
    }
  }
}

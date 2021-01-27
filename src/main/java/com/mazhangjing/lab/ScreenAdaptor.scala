package com.mazhangjing.lab

import com.mazhangjing.utils.Logging
import javafx.event.Event
import javafx.scene.Scene
import scalafx.delegate.SFXDelegate
import scalafx.scene.control.Label
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.text.Font

/**
  * 相比较 Screen 的抽象，此特质继承了 Screen，添加了全套的 LabUtils 鼠标和按键判断支持，使用 implicit 隐式注入
  * 使得 LabUtils 使用更简洁，此外提供了 ScalaFx 自动向 JavaFx 转换的快捷方法，注意 eventHandler 中的 event、scene
  * 仍然是 JavaFx 原生的！！最后，ScreenAdaptor 提供了 initScreen 和 eventHandler 的默认实现，避免 null 错误。
  */
trait ScreenAdaptor extends Screen with LabUtilsForScalaFx {
  protected implicit def exp: Experiment = getExperiment
  protected implicit def sce: Scene = getScene
  protected implicit val scr: Screen = this
  protected implicit def sfx2jfx(in:SFXDelegate[_]): Any = in.delegate

  protected val PASS:Unit = {}
  protected val UNLIMITED: Int = 10_000_000

  information = getClass.getSimpleName
  duration = UNLIMITED

  override def initScreen(): Screen = {
    layout = new StackPane {
      children = Seq(
        new Label(s"${information} have't implement initScreen!") {
          font = Font.font(30)
          textFill = Color.Red
        }
      )
    }
    this
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: Scene): Unit = PASS
}

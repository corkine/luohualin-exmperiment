package com.mazhangjing.utils

import java.io.{File, PrintWriter, StringWriter}
import java.nio.file.Paths
import java.util

import com.mazhangjing.lab.{ExpRunner, ExperimentHelper, OpenedEvent, SimpleExperimentHelperImpl}
import javafx.scene.control
import org.slf4j.LoggerFactory
import scalafx.beans.property.{BooleanProperty, DoubleProperty, IntegerProperty, StringProperty}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, CheckBox, Label, RadioButton, TextField, ToggleButton}
import scalafx.scene.image.Image
import scalafx.scene.layout.{GridPane, HBox}
import scalafx.scene.paint.Paint
import scalafx.stage.Stage

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait OutFocus {
  this: scalafx.scene.Node =>
  this.style = "-fx-focus-color: transparent;-fx-background-insets: 0; -fx-background-color: transparent, white, transparent, white;"
}

trait CenterV {
  this: scalafx.scene.layout.VBox =>
  this.padding = Insets(15)
  this.spacing = 15
  this.alignment = Pos.Center
}

trait CenterH {
  this: scalafx.scene.layout.HBox =>
  this.padding = Insets(15)
  this.spacing = 15
  this.alignment = Pos.Center
}

/*
class N
sealed class N10 extends N
sealed class N15 extends N
sealed class N20 extends N

class LeftV[T: ClassTag] {
  this: scalafx.scene.layout.VBox =>
  val number: Int =
    if (reflect.classTag[T].toString().contains("N10")) 10
    else if (reflect.classTag[T].toString().contains("N15")) 15
    else if (reflect.classTag[T].toString().contains("N20")) 20
    else 15
  this.padding = Insets(number)
  this.spacing = number
  this.alignment = Pos.CenterLeft
}*/

trait LeftV {
  this: scalafx.scene.layout.VBox =>
  this.padding = Insets(15)
  this.spacing = 15
  this.alignment = Pos.CenterLeft
}

trait LeftH {
  this: scalafx.scene.layout.HBox =>
  this.padding = Insets(15)
  this.spacing = 15
  this.alignment = Pos.CenterLeft
}

trait FastToggle {
  this: scalafx.scene.control.ToggleGroup =>
  def ! = {
    import scala.collection.JavaConverters._
    this.toggles.asScala.map(i => {
      if (i.getClass.equals(classOf[javafx.scene.control.RadioButton])) {
        new RadioButton(delegate = i.asInstanceOf[control.RadioButton])
      } else if (i.getClass.equals(classOf[javafx.scene.control.ToggleButton])) {
        new ToggleButton(delegate = i.asInstanceOf[javafx.scene.control.ToggleButton])
      } else null
      /*val tg = if (reflect.classTag[T].toString().contains("scalafx.scene.control.RadioButton")) {
        new ToggleButton(delegate = jn.asInstanceOf[javafx.scene.control.RadioButton])
      } else if (reflect.classTag[T].toString().contains("scalafx.scene.control.ToggleButton")) {
        new ToggleButton(delegate = jn.asInstanceOf[javafx.scene.control.ToggleButton])
      } else { null }*/
    })
  }
}

class ImagePattern(image:scalafx.scene.image.Image,
                   x:Double, y:Double,
                   width:Double, height:Double,
                   proportional:Boolean) extends {
  override val delegate: javafx.scene.paint.ImagePattern =
    new javafx.scene.paint.ImagePattern(image.delegate, x, y, width, height, proportional)
} with Paint(delegate) {
  def opaque: Boolean = delegate.isOpaque
}

object SFXUtils {

  def load(imageFolder:String, name:String): Try[File] = {
    val file = Paths.get(imageFolder, name).toFile
    if (file.exists()) Success(file)
    else Failure(new NoSuchElementException("没有此文件"))
  }

  def alert(info:String): Unit = {
    new Alert(AlertType.Information) {
      headerText = "警告"
      contentText = info
    }.showAndWait()
  }

  def goBtn(doIt: => Unit): HBox = {
    new HBox {
      padding = Insets(0,0,20,0)
      alignment = Pos.Center
      children = Seq(
        new Button("确定") {
          minWidth = 100
          minHeight = 30
          onAction = _ => {
            doIt
          }
        }
      )
    }
  }
}

object GridUtils {

  private val logger = LoggerFactory.getLogger(getClass)

  def line(lab:String, variable:Int, line: Int)
         (op: Int => Unit)
         (implicit gridPane: GridPane): Unit = {
    val label = new Label(lab)
    val textField = new TextField {
      text = variable.toString
      promptText = lab
      text.addListener(_ => {
        op(text().toInt)
      })
    }
    GridPane.setConstraints(label, 0, line)
    GridPane.setConstraints(textField, 1, line)
    gridPane.children.addAll(label, textField)
  }

  def line(lab:String, variable:Boolean, line: Int)
         (op: Boolean => Unit)
         (implicit gridPane: GridPane): Unit = {
    val label = new Label(lab)
    val choose = new CheckBox {
      selected = variable
      onAction = _ => op(selected())
    }
    GridPane.setConstraints(label, 0, line)
    GridPane.setConstraints(choose, 1, line)
    gridPane.children.addAll(label, choose)
  }

  def line(lab:String, variable:BooleanProperty, line: Int)
          (implicit gridPane: GridPane): Unit = {
    val label = new Label(lab)
    val choose = new CheckBox {
      selected <==> variable
    }
    GridPane.setConstraints(label, 0, line)
    GridPane.setConstraints(choose, 1, line)
    gridPane.children.addAll(label, choose)
  }

  def line(lab:String, variable:String, line: Int)
         (op: String => Unit)
         (implicit gridPane: GridPane): Unit = {
    val label = new Label(lab)
    val tf = new TextField {
      text = variable
      text.onChange {
        op(text())
      }
    }
    GridPane.setConstraints(label, 0, line)
    GridPane.setConstraints(tf, 1, line)
    gridPane.children.addAll(label, tf)
  }

  def line(lab:String, variable:StringProperty, line: Int)
          (implicit gridPane: GridPane): Unit = {
    val label = new Label(lab)
    val tf = new TextField {
      text <==> variable
    }
    GridPane.setConstraints(label, 0, line)
    GridPane.setConstraints(tf, 1, line)
    gridPane.children.addAll(label, tf)
  }

  def line(lab:String, variable:IntegerProperty, line: Int)
          (implicit gridPane: GridPane): Unit = {
    val label = new Label(lab)
    val tf = new TextField {
      text = variable().toString
      text.onChange {
        try {
          variable.set(text().toInt)
        } catch {
          case e: Throwable => logger.warn(s"转换 String ${text()} -> Int 出错 $e")
        }
      }
    }
    GridPane.setConstraints(label, 0, line)
    GridPane.setConstraints(tf, 1, line)
    gridPane.children.addAll(label, tf)
  }
}


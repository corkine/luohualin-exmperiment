import Exp1Config._
import com.mazhangjing.lab.{Experiment, Screen, ScreenAdaptor, Trial}
import com.mazhangjing.utils.Logging
import javafx.event.Event
import javafx.scene.input.KeyCode
import javafx.scene.{Scene => JScene}
import play.api.libs.json.{Json, Writes}
import scalafx.beans.property.StringProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, Text, TextAlignment}

import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ArrayBuffer

object ExpConfig {
  val IS_DEBUG = false
  var USER_ID: String = _
  var USER_MALE: Boolean = _
  lazy val SKIP_USER_INFO: Boolean = IS_DEBUG
  var SKIP_NOW = false
}

object Exp1Config {
  import ExpConfig._
  val INTRO_SIZE = 32
  lazy val BIG_INTRO_SIZE: Int = INTRO_SIZE + 10
  val CROSS_SIZE = 32
  val NUMBER_SIZE = 52
  val FEEDBACK_SIZE = 32
  val FEEDBACK_LEFT_PADDING = 250
  val EXP1_LEARN_BIG_INTRO_TIME = 500
  val EXP1_LEARN_NUMBER = "42364325"
  val EXP1_NUMBERS = Seq(
    "4638376136",
    "94362172263",
    "25735276275",
    "74352904325",
    "3721290482",
    "03237449324",
    "243094582375",
    "307294821639",
    "149532250")
  val CROSS_TIME = 250
  val NUMBER_TIME = 1000
}

object Exp1Data {
  case class Data(isPre:Boolean,
                  userAnswer:String,realAnswer:String,
                  answerRight:Boolean,
                  costMills:Long,
                  time:LocalDateTime = LocalDateTime.now())
  object Data {
    implicit val dataJSON: Writes[Data] = Json.writes[Data]
  }
  val data = new ArrayBuffer[Data]()
  def addData(newData:Data): Unit = data.addOne(newData)
}

class Exp1Trial extends Trial {
  override def initTrial(): Trial = {
    //指导语界面
    screens.add(new Intro {
      override val introSize: Int = INTRO_SIZE
      override val info: String =
        """屏幕上将会按顺序呈现一串数字，要求您记住数字串中呈现的最后4个数字，并在数字呈现完毕后在屏幕答题框内输入你记住的数字。 如果您已经理解了实验要求，请按 q 键开始练习；如果还没有理解，请咨询主试为您解释。
          |""".stripMargin
    }.initScreen())
    //练习部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "练习部分"
      override val timeSkip: Int = EXP1_LEARN_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    (1 to 10).foreach { _ =>
      //练习部分数字呈现
      val fullNumber = EXP1_LEARN_NUMBER
      fullNumber.toCharArray.zipWithIndex.foreach { case (c,n) =>
        val number = c.toString
        val count = n + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"Cross Screen[LEARN]"
        }.initScreen())
        screens.add(new Intro {
          override val introSize: Int = NUMBER_SIZE
          override val info: String = number
          override val textAlign: TextAlignment = TextAlignment.Center
          override val timeSkip: Int = NUMBER_TIME
          information = s"Number Screen[LEARN] $fullNumber - Index $count, Number $number"
        }.initScreen())
      }
      //练习部分数据收集
      screens.add(new AnswerCollect {
        override val realAnswer: String = fullNumber
        override val isPre: Boolean = true
        information = s"Number Check Screen[LEARN]"
      }.initScreen())
      //是否重试练习部分
      screens.add(new LearnTry {}.initScreen())
    }
    screens.add(new Normal {}.initScreen())
    //实验部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "实验部分"
      override val timeSkip: Int = EXP1_LEARN_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    EXP1_NUMBERS.foreach { fullNumber =>
      //实验部分数字呈现
      fullNumber.toCharArray.zipWithIndex.foreach { case (c,n) =>
        val number = c.toString
        val count = n + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"Cross Screen"
        }.initScreen())
        screens.add(new Intro {
          override val introSize: Int = NUMBER_SIZE
          override val info: String = number
          override val textAlign: TextAlignment = TextAlignment.Center
          override val timeSkip: Int = NUMBER_TIME
          information = s"Number Screen $fullNumber - Index $count, Number $number"
        }.initScreen())
      }
      //实验部分数据收集
      screens.add(new AnswerCollect {
        override val realAnswer: String = fullNumber
        override val isPre: Boolean = false
        information = s"Number Check Screen"
      }.initScreen())
    }
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "实验结束，感谢您的参与！"
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    this
  }
}

class LHLExperiment1 extends Experiment with Logging {
  override protected def initExperiment(): Unit = {
    //LearnTrial & NormalTrial
    trials.add(new Exp1Trial().initTrial())
  }

  override def saveData(): Unit = {
    log.info("Saving Data now...")
    try {
      val value = Json.toJson(Exp1Data.data)
      val userName = ExpConfig.USER_ID
      val userGender = if (ExpConfig.USER_MALE) "male" else "female"
      val writer = new FileWriter(s"EXP1_${userName}_${userGender}_" +
        s"${LocalDateTime.now()
          .format(DateTimeFormatter.ISO_DATE_TIME)
          .replace(":","_")}.json")
      writer.write(Json.prettyPrint(value))
      writer.flush()
      writer.close()
    } catch {
      case _: Throwable => log.warn("Save File Error.")
    }
  }
}

//单纯文字指导语 Screen
trait Intro extends ScreenAdaptor {
  override def callWhenShowScreen(): Unit = {
    if (ExpConfig.SKIP_NOW) goNextScreenSafe
  }
  val introSize:Int
  val info:String
  val skipKey:KeyCode = KeyCode.Q
  val timeSkip = 1000000
  val textAlign: TextAlignment = TextAlignment.Left
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text(info) {
          textAlignment = textAlign
          wrappingWidth <== sp.width / 2
          font = Font.font(introSize)
        }
      )
    }
    duration = timeSkip
    this
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    ifKeyButton(skipKey,event) {
      goNextScreenSafe
    }
  }
}

trait Normal extends ScreenAdaptor {
  override def callWhenShowScreen(): Unit = {
    ExpConfig.SKIP_NOW = false
  }
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text("正在加载正式实验...") {
          textAlignment = TextAlignment.Center
          wrappingWidth <== sp.width / 2
          font = Font.font(Exp1Config.BIG_INTRO_SIZE)
        }
      )
    }
    duration = 3000
    this
  }
}

//十字注视点 Screen
trait Cross extends ScreenAdaptor {
  override def callWhenShowScreen(): Unit = {
    if (ExpConfig.SKIP_NOW) goNextScreenSafe
  }
  val crossFontSize: Int
  val crossShowMs: Int
  val crossColor: Color = Color.Black
  override def initScreen(): Screen = {
    layout = new StackPane {
      children = Seq(
        new Label("+") {
          font = Font(crossFontSize)
          textFill = crossColor
        }
      )
    }.delegate
    duration = crossShowMs
    this
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = PASS
}

//练习重试 Screen
trait LearnTry extends ScreenAdaptor {
  override def callWhenShowScreen(): Unit = {
    if (ExpConfig.SKIP_NOW) goNextScreenSafe
  }
  val introSize:Int = INTRO_SIZE
  val info:String =
    """
      |对于刚才的任务是否需要再次练习？
      |Q 是的，再练习一次
      |P 不需要，进入正式实验
      |""".stripMargin
  val skipKey:KeyCode = KeyCode.Q
  val timeSkip = 1000000
  val textAlign: TextAlignment = TextAlignment.Center
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text(info) {
          textAlignment = textAlign
          wrappingWidth <== sp.width / 2
          font = Font.font(introSize)
        }
      )
    }
    duration = timeSkip
    this
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    ifKeyButton(skipKey,event) {
      goNextScreenSafe
    }
    ifKeyButton(KeyCode.P, event) {
      ExpConfig.SKIP_NOW = true
      goNextScreenSafe
    }
  }
}

//结果收集 Screen
trait AnswerCollect extends ScreenAdaptor {
  private var isMeSkip = false
  private var startTime: Long = _
  override def callWhenShowScreen(): Unit = {
    if (ExpConfig.SKIP_NOW) {
      isMeSkip = true; goNextScreenSafe
    } else startTime = System.currentTimeMillis()
  }
  val realAnswer: String
  val isPre: Boolean
  val timeSkip = 20000
  private val answer = StringProperty("")
  private val feedback = StringProperty("")
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new VBox {
          alignment = Pos.CenterLeft
          padding = Insets(0,0,0,Exp1Config.FEEDBACK_LEFT_PADDING)
          children = Seq(
            new Text {
              text = s"你有 ${timeSkip/1000}s 在屏幕中输入你记住的最后呈现的 4 位数字，" +
                s"如果提前写好了，请按空格键键继续实验。"
              font = Font.font(Exp1Config.FEEDBACK_SIZE)
              wrappingWidth <== sp.width / 2
            },
            new Label {
              text <==> answer
              font = Font.font(Exp1Config.FEEDBACK_SIZE)
            },
            new Label {
              text <==> feedback
              textFill = Color.Red
              font = Font.font(Exp1Config.FEEDBACK_SIZE)
            }
          )
        }
      )
    }
    duration = timeSkip
    this
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    ifKeyButton(KeyCode.SPACE, event) {
      if (answer().length == 4) goNextScreenSafe
      else feedback.set("未完成，请继续")
    }
    ifKeyButton(KeyCode.BACK_SPACE, event) {
      val now = answer.get()
      val len = now.length
      val next = if (len >= 1) now.substring(0, len - 1) else now
      answer.set(next)
    }
    ifKeyIn(event) { kc =>
      if (kc.isDigitKey || kc.isKeypadKey) {
        answer.set(answer.get() + kc.getName)
      }
    }
  }
  override def callWhenLeavingScreen(): Unit = {
    if (!isMeSkip) {
      val costTime = System.currentTimeMillis() - startTime
      val answerRight = realAnswer.endsWith(answer()) && answer().length == 4
      logger.info(s"UserAnswer ${answer()}, realAnswer $realAnswer, isRight?$answerRight")
      Exp1Data.addData(Exp1Data.Data(isPre, answer(), realAnswer, answerRight, costTime))
    }
  }
}
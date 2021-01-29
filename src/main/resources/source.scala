/**
 * @author Corkine Ma
 * @date 2021.01.27
 */
package com.mazhangjing.lhl

import com.mazhangjing.lab.{Experiment, Screen, ScreenAdaptor, Trial}
import com.mazhangjing.lhl.Exp1Config.{INTRO_SIZE, _}
import com.mazhangjing.utils.Logging
import com.typesafe.config.{Config, ConfigFactory}
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

import java.io.{File, FileOutputStream, FileWriter, OutputStreamWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util
import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.collection.mutable.ArrayBuffer
import com.mazhangjing.lab.{Experiment, Screen, ScreenAdaptor, Trial}
import com.mazhangjing.lhl.Exp2Config.{TARGET_SIZE, TARGET_TIME, _}
import com.mazhangjing.lhl.ExpConfig._SKIP_NOW
import com.mazhangjing.utils.Logging
import com.typesafe.config.Config
import javafx.event.Event
import javafx.scene.{Scene => JScene}
import net.ceedubs.ficus.Ficus._
import play.api.libs.json.{Json, Writes}
import scalafx.beans.property.StringProperty
import scalafx.scene.input.KeyCode
import scalafx.scene.layout.StackPane
import scalafx.scene.text.{Font, Text, TextAlignment}

import java.io.{FileOutputStream, FileWriter, OutputStreamWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import com.mazhangjing.lab.{Experiment, Screen, ScreenAdaptor, Trial}
import com.mazhangjing.lhl.Exp3Config.{GOOD_FOR_JUST_MIN_TRY, GOOD_PERCENT, GOOD_PERCENT_MIN_TRY, INTRO_SIZE, _PERCENT_NOW, _}
import com.mazhangjing.lhl.ExpConfig._SKIP_NOW
import com.mazhangjing.utils.Logging
import javafx.event.Event
import javafx.scene.paint.Color
import javafx.scene.{Scene => JScene}
import net.ceedubs.ficus.Ficus._
import play.api.libs.json.{Json, Writes}
import scalafx.beans.property.StringProperty
import scalafx.scene.input.KeyCode
import scalafx.scene.layout.StackPane
import scalafx.scene.text.{Font, Text, TextAlignment}

import java.io.{FileOutputStream, OutputStreamWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object ExpConfig {
  println("DIR IS " + System.getProperty("user.dir"))
  var CONF: Config = ConfigFactory.parseFile(new File("config.conf")).resolve()
  //var CONF: Config = ConfigFactory.load().resolve()
  var IS_DEBUG: Boolean = CONF.getBoolean("main.isDebug")
  var USER_ID: String = CONF.getString("main.fakeUserId")
  var USER_MALE: Boolean = CONF.getBoolean("main.fakeUserMale")
  var SKIP_USER_INFO: Boolean = CONF.getBoolean("main.skipUserInfo")
  var _SKIP_NOW = false
}

object Exp1Config {
  import ExpConfig._
  var INTRO_SIZE: Int = CONF.getInt("exp1.introSize")
  var BIG_INTRO_SIZE: Int = CONF.getInt("exp1.bigIntroSize")
  var CROSS_SIZE: Int = CONF.getInt("exp1.crossSize")
  var NUMBER_SIZE: Int = CONF.getInt("exp1.numberSize")
  var FEEDBACK_SIZE: Int = CONF.getInt("exp1.feedBackSize")
  var FEEDBACK_LEFT_PADDING: Int = CONF.getInt("exp1.feedBackLeftPadding")
  var EXP1_BIG_INTRO_TIME: Int = CONF.getInt("exp1.bigIntroTime")
  var EXP1_LEARN_NUMBER: String = CONF.getString("exp1.learnNumber") //42364325
  var EXP1_NUMBERS: util.List[String] = CONF.getStringList("exp1.numbers")
  /*Seq("4638376136","94362172263","25735276275","74352904325","3721290482",
    "03237449324","243094582375","307294821639","149532250")*/
  var CROSS_TIME: Int = CONF.getInt("exp1.crossTime")
  var NUMBER_TIME: Int = CONF.getInt("exp1.numberTime")

  var INTRO_CONTENT:String = CONF.getString("exp1.introContent")
}

object Exp1Data {
  case class Data(isPre:Boolean,
                  userAnswer:String,realAnswer:String,realShortAnswer:String,
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
      override val info: String = INTRO_CONTENT
    }.initScreen())
    //练习部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "练习部分"
      override val timeSkip: Int = EXP1_BIG_INTRO_TIME
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
          information = s"com.mazhangjing.lhl.Cross Screen[LEARN]"
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
      override val timeSkip: Int = EXP1_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    EXP1_NUMBERS.iterator().foreach { fullNumber =>
      //实验部分数字呈现
      fullNumber.toCharArray.zipWithIndex.foreach { case (c,n) =>
        val number = c.toString
        val count = n + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"com.mazhangjing.lhl.Cross Screen"
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
      val fileName = s"EXP1_${userName}_${userGender}_" +
        s"${LocalDateTime.now()
          .format(DateTimeFormatter.ISO_DATE_TIME)
          .replace(":","_")}.json"
      val writer = new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8")
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
    if (ExpConfig._SKIP_NOW) goNextScreenSafe
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
    ExpConfig._SKIP_NOW = false
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
    if (ExpConfig._SKIP_NOW) goNextScreenSafe
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
    if (ExpConfig._SKIP_NOW) goNextScreenSafe
  }
  val introSize:Int = INTRO_SIZE
  val info:String =
    """
      |对于刚才的任务是否需要再次练习？
      |
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
      ExpConfig._SKIP_NOW = true
      goNextScreenSafe
    }
  }
}

//结果收集 Screen
trait AnswerCollect extends ScreenAdaptor {
  private var isMeSkip = false
  private var startTime: Long = _
  override def callWhenShowScreen(): Unit = {
    if (ExpConfig._SKIP_NOW) {
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
      val realShortAnswer = realAnswer.substring(realAnswer.length - 4)
      logger.info(s"UserAnswer ${answer()}, realAnswer $realAnswer, isRight?$answerRight")
      Exp1Data.addData(Exp1Data.Data(isPre, answer(), realAnswer, realShortAnswer, answerRight, costTime))
    }
  }
}

object Exp2Config {
  import ExpConfig._
  private val CONF2: Config = CONF.getConfig("exp2")
  val INTRO_SIZE: Int = CONF2.as[Int]("introSize")
  val BIG_INTRO_SIZE: Int = CONF2.as[Int]("bigIntroSize")
  val TARGET_SIZE: Int = CONF2.as[Int]("targetSize")
  val CROSS_SIZE: Int = CONF2.as[Int]("crossSize")
  val EXP2_BIG_INTRO_TIME: Int = CONF2.as[Int]("bigIntroTime")
  val EXP2_LEARN_TARGETS: String = CONF2.as[Option[String]]("learnTargets").getOrElse("124abc")
  val EXP2_LEARN_ANSWERS: String = CONF2.as[Option[String]]("learnAnswers").getOrElse("sjjsjj")
  val EXP2_BLOCK1: String = CONF2.as[Option[String]]("block1").getOrElse("1 2 3 4 5 6 7 8 9 10")
  val EXP2_BLOCK1_ANSWER: String = CONF2.as[Option[String]]("block1Answer").getOrElse("s j s j s j s j s j")
  val EXP2_BLOCK2: String = CONF2.as[Option[String]]("block2").getOrElse("1 2 3 4 5 6 7 8 9 10 a e i o u b c d f g")
  val EXP2_BLOCK2_ANSWER: String = CONF2.as[Option[String]]("block2Answer").getOrElse("s j s j s j s j s j s s s s s j j j j j")
  val EXP2_BLOCK3: String = CONF2.as[Option[String]]("block3").getOrElse("a e i o u b c d f g")
  val EXP2_BLOCK3_ANSWER: String = CONF2.as[Option[String]]("block3Answer").getOrElse("s s s s s j j j j j")
  val CROSS_TIME: Int = CONF2.as[Int]("crossTime")
  val TARGET_TIME: Int = CONF2.as[Int]("targetTime")
  val FEEDBACK_TIME: Int = CONF2.as[Int]("feedBackTime")
  val GOOD_PERCENT: Double = CONF2.as[Double]("goodPercent")
  val GOOD_PERCENT_MIN_TRY: Int = CONF2.as[Int]("goodPercentMinTry")
  val INTRO_CONTENT: String = CONF2.as[String]("introContent")
  var _PERCENT_NOW = 0.0
}

object Exp2Data {
  case class Data(isPre:Boolean,
                  userAnswer:String,realAnswer:String,
                  answerRight:Boolean,
                  timeCost:Long,
                  blockInfo:String,
                  time:LocalDateTime = LocalDateTime.now())
  object Data {
    implicit val dataJSON: Writes[Data] = Json.writes[Data]
  }
  val data = new ArrayBuffer[Data]()
  def addData(newData:Data): Unit = data.addOne(newData)
}

class Exp2Trial extends Trial {
  override def initTrial(): Trial = {
    //指导语界面
    screens.add(new Intro {
      override val introSize: Int = INTRO_SIZE
      override val info: String = INTRO_CONTENT
    }.initScreen())
    //练习部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "练习部分"
      override val timeSkip: Int = EXP2_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    (1 to 20).foreach { _ =>
      //练习部分目标呈现和按键判断
      val fullTargets = EXP2_LEARN_TARGETS
      val answerTargets = EXP2_LEARN_ANSWERS
      fullTargets.toCharArray.zipWithIndex.foreach { case (c,n) =>
        val answerKey = answerTargets(n).toString
        val targetKey = c.toString
        val count = n + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"Cross Screen[LEARN]"
        }.initScreen())
        screens.add(new TargetShowAndCheckScreen {
          override val blockInfo: String = "PRE"
          override val target: String = targetKey
          override val isS: Boolean = answerKey.toUpperCase == "S"
          override val isLearn: Boolean = true
          information = s"Target Screen[LEARN] $fullTargets - Index $count, Target $targetKey, Answer $answerKey"
        }.initScreen())
      }
      screens.add(new FeedbackScreen {
        override val introSize: Int = INTRO_SIZE
        override val timeSkip: Int = FEEDBACK_TIME
        information = "Hint Screen[LEARN]"
      }.initScreen())
    }
    screens.add(new Normal {}.initScreen())
    //实验部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "实验部分"
      override val timeSkip: Int = EXP2_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    //BLOCK1 & 2 & 3
    Seq((EXP2_BLOCK1, EXP2_BLOCK1_ANSWER, "BLOCK1"),
      (EXP2_BLOCK2, EXP2_BLOCK2_ANSWER, "BLOCK2"),
      (EXP2_BLOCK3, EXP2_BLOCK3_ANSWER, "BLOCK3")).foreach { case (block_data, block_answer, block_info) =>
      val targets = block_data.split(" ").map(_.trim)
      val answers = block_answer.split(" ").map(_.trim)
      val all = Random.shuffle((targets zip answers).toBuffer)
      all.zipWithIndex.foreach { case ((t,a),i) =>
        val show = t
        val answer = a.toUpperCase()
        val index = i + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"com.mazhangjing.lhl.Cross Screen"
        }.initScreen())
        screens.add(new TargetShowAndCheckScreen {
          override val blockInfo: String = block_info
          override val target: String = show
          override val isS: Boolean = a.toUpperCase == "S"
          override val isLearn: Boolean = false
          information = s"Target Screen[$block_info] - Index $index, Target $show, Answer $answer"
        }.initScreen())
      }
    }
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "实验结束，感谢您的参与！"
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    this
  }
}

class LHLExperiment2 extends Experiment with Logging {
  override protected def initExperiment(): Unit = {
    //LearnTrial & NormalTrial
    trials.add(new Exp2Trial().initTrial())
  }

  override def saveData(): Unit = {
    log.info("Saving Data now...")
    try {
      val value = Json.toJson(Exp2Data.data)
      val userName = ExpConfig.USER_ID
      val userGender = if (ExpConfig.USER_MALE) "male" else "female"
      val fileName = s"EXP2_${userName}_${userGender}_" +
        s"${LocalDateTime.now()
          .format(DateTimeFormatter.ISO_DATE_TIME)
          .replace(":","_")}.json"
      val writer = new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8")
      writer.write(Json.prettyPrint(value))
      writer.flush()
      writer.close()
    } catch {
      case _: Throwable => log.warn("Save File Error.")
    }
  }
}

trait FeedbackScreen extends ScreenAdaptor {
  override def callWhenShowScreen(): Unit = {
    if (_SKIP_NOW) goNextScreenSafe
    else {
      if (goodToGo()) _SKIP_NOW = true
      info.set(s"当前正确率 ${String.format("%.2f",_PERCENT_NOW * 100)}%, " +
        s"需要达到的正确率 ${String.format("%.2f",GOOD_PERCENT * 100)}%, " +
        s"按 Q ${if (_PERCENT_NOW >= GOOD_PERCENT) "开始正式试验" else "重试"}")
    }
  }
  val goodPercent: Double = Exp2Config.GOOD_PERCENT
  val minTry: Int = Exp2Config.GOOD_PERCENT_MIN_TRY
  private val info = StringProperty("")
  val introSize: Int
  val timeSkip: Int
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text {
          text <== info
          textAlignment = TextAlignment.Center
          wrappingWidth <== sp.width / 2
          font = Font.font(INTRO_SIZE)
        }
      )
    }
    duration = timeSkip
    this
  }
  private def goodToGo(): Boolean = {
    val now = Exp2Data.data
    val all = now.length
    val rightPercent = now.count(_.answerRight) * 1.0 / all
    logger.info(s"Right Percent $rightPercent now... Target Percent is $goodPercent")
    _PERCENT_NOW = rightPercent
    if (rightPercent >= goodPercent && all >= minTry) true else false
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    ifKeyButton(KeyCode.Q, event) {
      goNextScreenSafe
    }
  }
}

trait TargetShowAndCheckScreen extends ScreenAdaptor {
  val target: String
  val isS: Boolean
  val isLearn:Boolean
  val blockInfo: String
  private var startTime: Long = 0L
  override def callWhenShowScreen(): Unit = {
    if (_SKIP_NOW) goNextScreenSafe
    else startTime = System.currentTimeMillis()
  }
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text(target) {
          textAlignment = TextAlignment.Center
          wrappingWidth <== sp.width / 2
          font = Font.font(TARGET_SIZE)
        }
      )
    }
    duration = TARGET_TIME
    this
  }

  override def callWhenLeavingScreen(): Unit = {
    if (!_SKIP_NOW) {
      val timeCost = System.currentTimeMillis() - startTime
      val userChoose = getCode
      if (getCode.isEmpty) logger.warn("User don't have answer!!!")
      val answerRight = if (userChoose.toUpperCase().contains("S") && isS) true
      else if (userChoose.toUpperCase().contains("J") && !isS) true
      else false
      val rightAnswer = if (isS) "S" else "J"
      val data = Exp2Data.Data(isLearn, userChoose, rightAnswer, answerRight, timeCost, blockInfo)
      logger.info(s"UserChoose $userChoose, rightAnswer $rightAnswer, AnswersRight $answerRight")
      Exp2Data.addData(data)
    }
  }

  private var getCode: String = ""

  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    if (!_SKIP_NOW) {
      ifKeyIn(event) { code =>
        getCode = code.getName
        goNextScreenSafe
      }
    }
  }
}

object Exp3Config {
  import ExpConfig._
  private val CONF3 = CONF.getConfig("exp3")
  val INTRO_SIZE: Int = CONF3.as[Int]("introSize")
  val BIG_INTRO_SIZE: Int = CONF3.as[Int]("bigIntroSize")
  val TARGET_SIZE: Int = CONF3.as[Int]("targetSize")
  val CROSS_SIZE: Int = CONF3.as[Int]("crossSize")
  val EXP3_BIG_INTRO_TIME: Int = CONF3.as[Int]("bigIntroTime")
  val EXP3_LEARN_TARGETS: String = CONF3.as[Option[String]]("learnTargets").getOrElse("R-# Y-% B-& Y-黄 B-蓝 G-绿 B-绿 G-红 R-黄") //Y B G G R Y
  val EXP3_LEARN_ANSWERS: String = CONF3.as[Option[String]]("learnAnswers").getOrElse("s d j d j k j k s")
  val EXP3_BLOCK1: String = CONF3.as[Option[String]]("block1").getOrElse("R-# Y-% B-& G-$ Y-# G-% B-& R-$ G-# B-% R-& Y-$")
  val EXP3_BLOCK1_ANSWER: String = CONF3.as[Option[String]]("block1Answer").getOrElse("s d j k d k j s k j s d")
  val EXP3_BLOCK2: String = CONF3.as[Option[String]]("block2").getOrElse("R-黄 Y-蓝 B-绿 G-红 Y-绿 B-红 G-黄 R-蓝 B-黄 G-蓝 R-绿 Y-红")
  val EXP3_BLOCK2_ANSWER: String = CONF3.as[Option[String]]("block2Answer").getOrElse("s d j k d j k s j k s d")
  val CROSS_TIME: Int = CONF3.as[Int]("crossTime")
  val TARGET_TIME: Int = CONF3.as[Int]("targetTime")
  val FEEDBACK_TIME: Int = CONF3.as[Int]("feedBackTime")
  val GOOD_PERCENT: Double = CONF3.as[Double]("goodPercent")
  val GOOD_PERCENT_MIN_TRY: Int = CONF3.as[Int]("goodPercentMinTry")
  val GOOD_FOR_JUST_MIN_TRY: Boolean = CONF3.as[Boolean]("goodForJustMinTry")
  val INTRO_CONTENT: String = CONF3.as[String]("introContent")
  def color: String => Color = (s:String ) => s.toUpperCase match {
    case "R" => Color.RED
    case "Y" => Color.YELLOW
    case "B" => Color.BLUE
    case "G" => Color.GREEN
  }
  var _PERCENT_NOW = 0.0
  var _USE_JI_MAO_MAO_JI = true
}

object Exp3Data {
  case class Data(isPre:Boolean,
                  target:String, targetColor:String,
                  userAnswer:String,realAnswer:String,
                  answerRight:Boolean,
                  timeCost:Long,
                  blockInfo:String,
                  time:LocalDateTime = LocalDateTime.now())
  object Data {
    implicit val dataJSON: Writes[Data] = Json.writes[Data]
  }
  val data = new ArrayBuffer[Data]()
  def addData(newData:Data): Unit = data.addOne(newData)
}

class Exp3Trial extends Trial {
  override def initTrial(): Trial = {
    //指导语界面
    screens.add(new Intro {
      override val introSize: Int = INTRO_SIZE
      override val info: String = INTRO_CONTENT
    }.initScreen())
    //练习部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "练习部分"
      override val timeSkip: Int = EXP3_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    (1 to 20).foreach { _ =>
      //练习部分目标呈现和按键判断
      val fullTargets = EXP3_LEARN_TARGETS
      val answerTargets = EXP3_LEARN_ANSWERS
      val answers = answerTargets.split(" ")
      fullTargets.split(" ").map { kc =>
        val k_c = kc.trim.split("-"); (k_c(0), k_c(1))}.zipWithIndex.foreach { case ((k,c),n) =>
        val targetKey = c
        val answerKey = answers(n).toUpperCase
        val currColor = color(k)
        val count = n + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"com.mazhangjing.lhl.Cross Screen[LEARN]"
        }.initScreen())
        screens.add(new StoopScreen {
          override val blockInfo: String = "PRE"
          override val target: String = targetKey
          override val targetColor: Color = currColor
          override val rightKey: String = answerKey
          override val isLearn: Boolean = true
          information = s"Target Screen[LEARN] $fullTargets - Index $count, Target $targetKey, Answer $answerKey"
        }.initScreen())
      }
      screens.add(new StoopFeedbackScreen {
        override val introSize: Int = INTRO_SIZE
        override val timeSkip: Int = FEEDBACK_TIME
        information = "Hint Screen[LEARN]"
      }.initScreen())
    }
    screens.add(new Normal {}.initScreen())
    //实验部分提示界面
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "实验部分"
      override val timeSkip: Int = EXP3_BIG_INTRO_TIME
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    (if (_USE_JI_MAO_MAO_JI)
      Seq((EXP3_BLOCK1, EXP3_BLOCK1_ANSWER, "BLOCK1基线"),
        (EXP3_BLOCK2, EXP3_BLOCK2_ANSWER, "BLOCK2矛盾"),
        (EXP3_BLOCK2, EXP3_BLOCK2_ANSWER, "BLOCK3矛盾"),
        (EXP3_BLOCK1, EXP3_BLOCK1_ANSWER, "BLOCK4基线"))
    else
      Seq((EXP3_BLOCK2, EXP3_BLOCK2_ANSWER, "BLOCK1矛盾"),
        (EXP3_BLOCK1, EXP3_BLOCK1_ANSWER, "BLOCK2基线"),
        (EXP3_BLOCK1, EXP3_BLOCK1_ANSWER, "BLOCK3基线"),
        (EXP3_BLOCK2, EXP3_BLOCK2_ANSWER, "BLOCK4矛盾"))).foreach { case (block_data, block_answer, block_info) =>
      val targets = block_data.split(" ").map { kc =>
        val k_c = kc.trim.split("-"); (k_c(0), k_c(1))
      }
      val answers = block_answer.split(" ").map(_.trim)
      //实验 3 正式试验的每个 trial 出现 2 次
      val all = Random.shuffle((targets zip answers).toBuffer ++ (targets zip answers).toBuffer)
      all.zipWithIndex.foreach { case (((c, t), r), i) =>
        val show = t
        val showColor = color(c)
        val answer = r.toUpperCase()
        val index = i + 1
        screens.add(new Cross {
          override val crossShowMs: Int = CROSS_TIME
          override val crossFontSize: Int = CROSS_SIZE
          information = s"com.mazhangjing.lhl.Cross Screen"
        }.initScreen())
        screens.add(new StoopScreen {
          override val blockInfo: String = block_info
          override val target: String = show
          override val targetColor: Color = showColor
          override val rightKey: String = answer
          override val isLearn: Boolean = false
          information = s"Target Screen[$block_info] - Index $index, Target $target, Answer $answer"
        }.initScreen())
      }
    }
    screens.add(new Intro {
      override val introSize: Int = BIG_INTRO_SIZE
      override val info: String = "实验结束，感谢您的参与！"
      override val textAlign: TextAlignment = TextAlignment.Center
    }.initScreen())
    this
  }
}

class LHLExperiment3 extends Experiment with Logging {
  override protected def initExperiment(): Unit = {
    //LearnTrial & NormalTrial
    trials.add(new Exp3Trial().initTrial())
  }

  override def saveData(): Unit = {
    log.info("Saving Data now...")
    try {
      val value = Json.toJson(Exp3Data.data)
      val userName = ExpConfig.USER_ID
      val userGender = if (ExpConfig.USER_MALE) "male" else "female"
      val fileName = s"EXP3_${if (_USE_JI_MAO_MAO_JI) "JMMJ" else "MJJM"}_" +
        s"${userName}_${userGender}_" +
        s"${LocalDateTime.now()
          .format(DateTimeFormatter.ISO_DATE_TIME)
          .replace(":","_")}.json"
      val writer = new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8")
      writer.write(Json.prettyPrint(value))
      writer.flush()
      writer.close()
    } catch {
      case _: Throwable => log.warn("Save File Error.")
    }
  }
}

trait StoopScreen extends ScreenAdaptor {
  val target: String
  val targetColor: Color
  val rightKey: String
  val isLearn:Boolean
  val blockInfo: String
  private var startTime: Long = 0L
  override def callWhenShowScreen(): Unit = {
    if (_SKIP_NOW) goNextScreenSafe
    else startTime = System.currentTimeMillis()
  }
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text(target) {
          textAlignment = TextAlignment.Center
          wrappingWidth <== sp.width / 2
          font = Font.font(TARGET_SIZE)
          fill = new scalafx.scene.paint.Color(targetColor)
        }
      )
    }
    duration = TARGET_TIME
    this
  }

  override def callWhenLeavingScreen(): Unit = {
    if (!_SKIP_NOW) {
      val timeCost = System.currentTimeMillis() - startTime
      val userChoose = getCode.toUpperCase
      if (getCode.isEmpty) logger.warn("User don't have answer!!!")
      val answerRight = if (userChoose == rightKey) true else false
      val data = Exp3Data.Data(isLearn, target, targetColor.toString, userChoose, rightKey, answerRight, timeCost, blockInfo)
      logger.info(s"UserChoose $userChoose, rightAnswer $rightKey, AnswersRight $answerRight")
      Exp3Data.addData(data)
    }
  }

  private var getCode: String = ""

  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    if (!_SKIP_NOW) {
      ifKeyIn(event) { code =>
        getCode = code.getName
        goNextScreenSafe
      }
    }
  }
}

trait StoopFeedbackScreen extends ScreenAdaptor {
  override def callWhenShowScreen(): Unit = {
    if (_SKIP_NOW) goNextScreenSafe
    else {
      if (goodToGo()) _SKIP_NOW = true
      info.set(s"当前正确率 ${String.format("%.2f",_PERCENT_NOW * 100)}%, " +
        s"需要达到的正确率 ${String.format("%.2f",GOOD_PERCENT * 100)}%, " +
        s"按 Q ${if (_PERCENT_NOW >= GOOD_PERCENT) "开始正式试验" else "重试"}")
    }
  }
  val goodPercent: Double = Exp3Config.GOOD_PERCENT
  val minTry: Int = Exp3Config.GOOD_PERCENT_MIN_TRY
  private val info = StringProperty("")
  val introSize: Int
  val timeSkip: Int
  override def initScreen(): Screen = {
    layout = new StackPane { sp =>
      children = Seq(
        new Text {
          text <== info
          textAlignment = TextAlignment.Center
          wrappingWidth <== sp.width / 2
          font = Font.font(INTRO_SIZE)
        }
      )
    }
    duration = timeSkip
    this
  }
  private def goodToGo(): Boolean = {
    if (!GOOD_FOR_JUST_MIN_TRY) { //统计所有数据
      val now = Exp3Data.data
      val all = now.length
      val rightPercent = now.count(_.answerRight) * 1.0 / all
      logger.info(s"[FULL]Right Percent $rightPercent now... Target Percent is $goodPercent")
      _PERCENT_NOW = rightPercent
      if (rightPercent >= goodPercent && all >= minTry) true else false
    } else {
      val now = Exp3Data.data.reverse.take(GOOD_PERCENT_MIN_TRY)
      val all = now.length
      val rightPercent = now.count(_.answerRight) * 1.0 / all
      logger.info(s"[LIMIT]Right Percent $rightPercent now... Target Percent is $goodPercent")
      _PERCENT_NOW = rightPercent
      if (rightPercent >= goodPercent && all >= minTry) true else false
    }
  }
  override def eventHandler(event: Event, experiment: Experiment, scene: JScene): Unit = {
    ifKeyButton(KeyCode.Q, event) {
      goNextScreenSafe
    }
  }
}

//plugins.sbt
/*logLevel := Level.Debug
addSbtPlugin("com.eed3si9n"  %  "sbt-assembly"  %  "0.14.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.6")*/

//build.sbt
/*name := "lhlExp"
description := "罗华林的毕业实验"
version := "1.1"
fork in run := true
mainClass in (Compile, run) := Some("com.mazhangjing.lhl.RunnableApp")
scalaVersion := "2.13.1"
javacOptions := Seq("-target", "1.8")

// https://mvnrepository.com/artifact/org.scalafx/scalafx
libraryDependencies += "org.scalafx" %% "scalafx" % "12.0.2-R18"
// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.1" % Test
// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25"
// https://mvnrepository.com/artifact/ch.qos.logback/logback-core
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.3"
// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// https://mvnrepository.com/artifact/com.typesafe.play/play-json
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"
// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.8.0"
// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.4.0"
libraryDependencies += "com.iheart" %% "ficus" % "1.5.0"


mainClass in assembly := Some("com.mazhangjing.lhl.RunnableApp")
assemblyMergeStrategy in assembly := {
  case manifest if manifest.contains("MANIFEST.MF") =>
    MergeStrategy.discard
  case moduleInfo if moduleInfo.contains("module-info.class") =>
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
    MergeStrategy.concat
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val winpkg = taskKey[Unit]("jar 打包 exe")
winpkg := {
  //确保 launchPath 包含 launch4j 安装程序
  //确保 target/scala-2.13 生成的 jar 文件名称和 package.xml 文件内对应，确保 package.xml 中 output 文件夹存在且具有 JRE
  val launchPath = "C:\\Program Files (x86)\\Launch4j"
  val pkg_config = "C:\\Users\\Corkine\\Desktop\\lhlExp\\target\\scala-2.13\\package.xml"
  val output = "C:\\Users\\Corkine\\Desktop\\lhlExperiment_output"
  println(s"cmd /c C: && cd $launchPath && .\\launch4j.jar $pkg_config")
  val ans = s"cmd /c C: && cd $launchPath && .\\launch4j.jar $pkg_config && explorer $output".!!
  println(ans)
}*/

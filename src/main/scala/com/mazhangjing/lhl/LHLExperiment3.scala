package com.mazhangjing.lhl

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
package com.mazhangjing.lhl

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
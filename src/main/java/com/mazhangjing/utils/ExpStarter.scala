package com.mazhangjing.utils

import java.util

import com.mazhangjing.lab.{ExpRunner, ExperimentHelper, OpenedEvent, SimpleExperimentHelperImpl}
import scalafx.stage.Stage

trait ExpStarter {
  def runExperiment(experimentClassName:String,
                    fullScreen: Boolean,
                    stage: Stage): Unit = {
    val helper: ExperimentHelper = new SimpleExperimentHelperImpl(new ExpRunner {
      override def initExpRunner(): Unit = {
        setEventMakerSet(null)
        val set = new util.HashSet[OpenedEvent]()
        set.add(OpenedEvent.KEY_PRESSED)
        setOpenedEventSet(set)
        setExperimentClassName(experimentClassName)
        setTitle("Experiment")
        setVersion("[MVST]")
        setFullScreen(fullScreen)
      }
    })
    helper.initStage(stage)
    stage.setFullScreen(fullScreen)
    stage.show()
  }
}

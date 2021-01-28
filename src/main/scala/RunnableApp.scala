import com.mazhangjing.utils.{ExpStarter, Logging}
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.StringProperty
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}

object RunnableApp extends JFXApp with Logging {
  stage = new PrimaryStage { mainStage =>
    mainStage.delegate.setOnCloseRequest { _ => System.exit(0) }
    //获取当前用户信息
    if (!ExpConfig.SKIP_USER_INFO) {
      val USER_ID = StringProperty("")
      val USER_GENDER = StringProperty("")
      def infoPane: Alert = new Alert(AlertType.Information) {
        headerText = "输入被试信息以继续"
        dialogPane.get().setContent(
          new VBox {
            spacing = 7
            alignment = Pos.Center
            children = Seq(
              new HBox {
                children = Seq(Label("编号"), new TextField() {
                  text <==> USER_ID
                })
                spacing = 7
              },
              new HBox {
                spacing = 7
                children = Seq(Label("性别"), new ComboBox[String](Seq("男", "女")) {
                  selectionModel().selectedItemProperty().addListener((_,_,n) => {
                    USER_GENDER.set(n)
                  })
                })
              })
          }
        )
      }
      while (USER_GENDER().isEmpty || USER_ID().isEmpty) infoPane.showAndWait()
      ExpConfig.USER_ID = USER_ID()
      ExpConfig.USER_MALE = if (USER_GENDER().contains("男")) true else false
      log.info(s"Get UserID ${ExpConfig.USER_ID}, UserIsMale? ${ExpConfig.USER_MALE}")
    } else {
      log.warn("Use Fake UserID and UserMale...")
    }

    scene = new Scene(400,300) {
      root = new VBox {
        padding = Insets(0,0,0,70)
        alignment = Pos.CenterLeft
        spacing = 10
        children = Seq(
          new Button("实验1") {
            onAction = _ => {
              new ExpStarter{}
                .runExperiment("LHLExperiment1",fullScreen = !ExpConfig.IS_DEBUG,mainStage)
            }
          },
          new Button("实验2") {
            onAction = _ => {
              new ExpStarter {}
                .runExperiment("LHLExperiment2",fullScreen = !ExpConfig.IS_DEBUG, mainStage)
            }
          },
          new Button("实验3 JMMJ") {
            onAction = _ => {
              Exp3Config._USE_JI_MAO_MAO_JI = true
              new ExpStarter {}
                .runExperiment("LHLExperiment3",fullScreen = !ExpConfig.IS_DEBUG, mainStage)
            }
          },
          new Button("实验3 MJJM") {
            onAction = _ => {
              Exp3Config._USE_JI_MAO_MAO_JI = false
              new ExpStarter {}
                .runExperiment("LHLExperiment3",fullScreen = !ExpConfig.IS_DEBUG, mainStage)
            }
          }
        )
      }
    }
  }
}

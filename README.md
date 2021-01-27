# Experiment
src/main/scala 为代码目录，项目为 sbt 构建，依赖 JDK8。src/main/java 为 Psy4J 框架源代码目录。

程序使用了如下语言、平台和技术：

![](http://static2.mazhangjing.com/badge/openjdk.png)
![](http://static2.mazhangjing.com/badge/javafx.png)
![](http://static2.mazhangjing.com/badge/scala.png)
![](http://static2.mazhangjing.com/badge/python.png)

## 开发必须技术
### 能运行代码所必须的技术
- JDK 安装和运行相关知识 [官方网站](https://www.oracle.com/java/technologies/javase-downloads.html)
- sbt（Simple Build Tools): Scala/Java 工程构建知识 [官方文档](https://www.scala-sbt.org/1.x/docs/)

### 理解更改调试项目所需的技术
- Scala 语言（刺激生成）[官方网站](https://www.scala-lang.org/)
- Java 语言（基本框架）[官方网站](https://www.oracle.com/java/technologies/javase-downloads.html)
- Python 脚本语言（数据分析）[官方网站](https://www.python.org/)
- sbt 构建工具（工程构建）[官方网站](https://www.scala-sbt.org)
- Maven 仓库概念（工程构建）[官方网站](http://maven.apache.org/)
- Play JSON 包（JVM，JSON数据格式转换）[项目站点](https://github.com/playframework/play-json)
- Logback 包（JVM，日志记录）[项目站点](http://logback.qos.ch/)
- ScalaFX 包（JVM，GUI呈现包装）[项目站点](https://github.com/scalafx/scalafx)
- JavaFx 包（JVM，GUI呈现基础）[项目站点](https://docs.oracle.com/javase/8/javafx/api/)
- ScalaTest 包（JVM，测试）[项目站点](https://www.scalatest.org/)
- JsonLab 包（MATLAB，JSON数据格式转换）[项目站点](https://github.com/fangq/jsonlab)

## 注意事项

最近版本的 Psy4J 经过修改，可以较好的整合 Scala Trait—Mixin 风格开发，同时配合 ScalaFx 进行工作。各个类进行了一定的修正，现在能够配合 Scala Trait 使用（Java protected 和 Scala 继承的语义有差异，因此调整了常用 API 的关键词，设定为公开，比如 Trial 的 screens、logger 等）。此外，扩展了 Scala 的 ScreenAdaptor，添加了常用的隐式注入、方法以及混入了 LabUtils，提供更加简捷的 API 使用体验（JavaFx 事件判断、定时器任务等）。

### Psy4J 自身的建议：

- Psy4J 内置了 logger，使用 Logback/Slf4J 日志库，可直接使用。

- Psy4J 定时器是单线程的，因此请勿同时启用多个定时器，最好嵌套调用，在外层回调函数中创建新的定时器。

### Psy4J 和 Scala 的整合建议

- Experiment 的实现类不能为 trait，因为 Java 把 trait 作为 Interface 对待，而 Experiment 是通过反射调用的，自然不能初始化，正确的做法是设置为 class —— 这从语义上来说也是正确的。

- Screen 的实现类推荐使用 trait，配置项和 Screen 的依赖项可通过 trait mixin 混入 Config 来提供，这样的写法效率很高，不用频繁的修改构造器传入参数。

- 对于 Config 配置 trait，需要注意，当在一个 Trial 中使用它混入 Screen 构造 Screen 实例后，在 Trial 中继承并重载其方法不会影响 Screen 中 Config 的值。原因很简单，因为 trait mixin 和 Object 很不同，被 mixin 的 trait 均含有其拷贝，因此多个 mixin 同一 trait 的 trait 不共享这个 trait 的变量。这避免了大型程序中的变量可变导致的状态问题，但有时候，不由自主的会错误使用它：比如在 Trial 中混入 Config 修改一个值，希望这个值在其 Screen with Config 中生效 —— 这是不可能的。虽然它确实在一定程度上造成了不便。解决方法很简单，Screen 通过 Trial mixin 的 Config 字段来传入其需要 mixin 的值，或者在其自身创建一个 Config 的子类，重写某个字段，然后将其 mixin 给其自身的 Screen，虽然较麻烦，但还是比构造器传递参数来的方便。
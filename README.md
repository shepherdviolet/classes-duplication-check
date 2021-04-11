# Jar包类冲突检查

* 检查当前目录下所有的Jar包, 是否有同名类(不检查其他资源文件)
* 检查完在当前目录产生日志文件: classes-duplications-yyyyMMdd-HH-mm-ss.log

# 工具打包

* 执行: gradlew clean build shadowJar
* 从build/libs中提取工具包: classes-duplication-check-?.?-all.jar

# 工具使用

* 假设我们要检查一个目录下所有的jar包之间是否有类冲突(同名类)
* 将工具jar包放到目录下
* 执行工具jar包 (Windows双击, Linux执行 java -jar classes-duplication-check-?.?-all.jar)
* 稍等片刻, 获取日志文件classes-duplications-yyyyMMdd-HH-mm-ss.log即可

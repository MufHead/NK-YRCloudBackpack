plugins {
    id("java")
}

group = "com.yirankuma.yrcloudbackpack"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<JavaCompile> {
    //打包出的文件路径和名称
    options.encoding = "UTF-8"
}
// 自定义JAR任务，指定输出目录和文件名
tasks.jar {
    archiveBaseName.set("YRCloudBackpack")
    archiveVersion.set("")
    destinationDirectory.set(file("E:/ServerPLUGINS/网易NK服务器插件"))
    
    // 确保目录存在
    doFirst {
        destinationDirectory.get().asFile.mkdirs()
    }
}
plugins {
    id("java")
}

group = "com.yirankuma.yrcloudbackpack"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    // YRDatabase 依赖从 libs/YRDatabase.jar 引入，使用 compileOnly 避免打包冲突
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    sourceCompatibility = "18"
    targetCompatibility = "18"
}

tasks.jar {
    archiveBaseName.set("YRCloudBackpack")
    archiveVersion.set("")
    destinationDirectory.set(file("E:/ServerPLUGINS/网易NK服务器插件"))

    doFirst {
        destinationDirectory.get().asFile.mkdirs()
    }
}

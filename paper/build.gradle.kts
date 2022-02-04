repositories {
    maven(url = "https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    val paperVersion = "1.18.1-R0.1-SNAPSHOT"

    implementation(project(":kryo-player-sync-common"))

    implementation("com.github.kryoniteorg:kryo-messaging:2.0.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.3")
    implementation("com.zaxxer:HikariCP:5.0.1")

    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    testImplementation("io.papermc.paper:paper-api:$paperVersion")
    testImplementation("org.awaitility:awaitility:4.1.1")
}

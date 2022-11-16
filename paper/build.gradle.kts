repositories {
    maven(url = "https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    val paperVersion = "1.19.2-R0.1-SNAPSHOT"
    val vaultVersion = "1.7.1"

    implementation(project(":kryo-player-sync-common"))

    implementation("com.github.kryoniteorg:kryo-messaging:2.0.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly("com.github.MilkBowl:VaultAPI:$vaultVersion")

    testImplementation("io.papermc.paper:paper-api:$paperVersion")
    testImplementation("com.github.MilkBowl:VaultAPI:$vaultVersion")
    testImplementation("org.awaitility:awaitility:4.2.0")
}

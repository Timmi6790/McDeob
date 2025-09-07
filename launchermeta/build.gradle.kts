dependencies {
    implementation(project(":common", "shadow"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.0")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")

    implementation("com.google.dagger:dagger:2.57.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.52")
}
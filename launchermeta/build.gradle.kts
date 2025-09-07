dependencies {
    implementation(project(":common", "shadow"))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.okhttp)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)
}
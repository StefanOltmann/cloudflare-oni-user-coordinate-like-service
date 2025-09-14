import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile

plugins {
    kotlin("multiplatform") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
}

group = "de.stefan_oltmann"

repositories {
    mavenCentral()
}

kotlin {

    js(IR) {

        /* Important: Cloudflare workers are V8 isolates. It's *not* a Node.js environment. */
        nodejs()

        binaries.executable()
    }
}

tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        target.set("es2015")
    }
}

/**
 * This task adds the Cloudflare workers entry point.
 */
tasks.named("jsProductionExecutableCompileSync") {

    val entrypointFile = "${layout.buildDirectory.asFile.get()}/js/packages/${project.name}/kotlin/${project.name}.mjs"

    outputs.file(entrypointFile)

    /*
     * The Cloudflare workers expect this entry point.
     */
    val jsEntrypoint = """
            /* The entrypoint expected by Cloudflare */
            export default {
                fetch(request, env, ctx) {
                    return handleRequest(request, env, ctx);
                },
            };
        """.trimIndent()

    doLast {
        File(entrypointFile).appendText(jsEntrypoint)
    }
}

package lu.kremi151.prefgen

import lu.kremi151.prefgen.extensions.android
import lu.kremi151.prefgen.extensions.variants
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

internal class AndroidPrefgenPlugin: Plugin<Project> {

	override fun apply(project: Project) {
		val extension = project.extensions.create("prefgen", AndroidPrefgenPluginExtension::class.java)

		project.android.variants.all { variant ->
			val genTaskName = "generatePreferenceLinkSrc${variant.name.capitalize()}"

			val packageName = extension.packageName.get().ifBlank {
				variant.applicationId
			}

			val rootGenSrcPath = "${project.buildDir}/generated/source/${variant.dirName}"
			val outputDir = File("$rootGenSrcPath/${packageName.replace(".", "/")}").also {
				it.mkdirs()
			}
			val taskProvider = project.tasks.register(genTaskName, AndroidPrefgenTask::class.java) { genTask ->
				genTask.group = "prefgen"

				genTask.outputFile = File(outputDir, "PrefR.java")

				val xmlFiles = variant.sourceSets
					.flatMap { it.resDirectories }
					.map { File(it, "xml") }
					.filter { it.exists() && it.isDirectory }
					.flatMap { it.listFiles().toList() }
					.filter { it.isFile }

				genTask.inputFiles = xmlFiles

				genTask.packageName = packageName
			}

			variant.registerJavaGeneratingTask(taskProvider, File(rootGenSrcPath))
		}
	}

}
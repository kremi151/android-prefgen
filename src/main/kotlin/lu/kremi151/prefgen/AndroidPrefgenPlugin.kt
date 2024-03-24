package lu.kremi151.prefgen

import lu.kremi151.prefgen.extensions.android
import lu.kremi151.prefgen.extensions.variants
import lu.kremi151.prefgen.tasks.GenerateFragmentsTask
import lu.kremi151.prefgen.tasks.GeneratePrefRTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

internal class AndroidPrefgenPlugin: Plugin<Project> {

	companion object {
		private const val TASK_GROUP = "prefgen"
	}

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
			val prefRTaskProvider = project.tasks.register(genTaskName, GeneratePrefRTask::class.java) { genTask ->
				genTask.group = TASK_GROUP

				genTask.prefRFile = File(outputDir, "PrefR.java")

				val xmlFiles = variant.sourceSets
					.flatMap { it.resDirectories }
					.map { File(it, "xml") }
					.filter { it.exists() && it.isDirectory }
					.flatMap { it.listFiles().toList() }
					.filter { it.isFile }

				genTask.inputFiles = xmlFiles

				genTask.packageName = packageName
			}
			variant.registerJavaGeneratingTask(prefRTaskProvider, File(rootGenSrcPath))

			if (extension.generateFragments.getOrElse(true)) {
				val fragmentOutputDir = File(outputDir, "fragments")
				val fragmentTaskProvider = project.tasks.register(genTaskName, GenerateFragmentsTask::class.java) { genTask ->
					genTask.group = TASK_GROUP
					genTask.outputSourcesDir = fragmentOutputDir

					val xmlFiles = variant.sourceSets
						.flatMap { it.resDirectories }
						.map { File(it, "xml") }
						.filter { it.exists() && it.isDirectory }
						.flatMap { it.listFiles().toList() }
						.filter { it.isFile }

					genTask.inputFiles = xmlFiles

					genTask.packageName = "${packageName}.fragments"
				}
				variant.registerJavaGeneratingTask(fragmentTaskProvider, File(rootGenSrcPath))
			}
		}
	}

}
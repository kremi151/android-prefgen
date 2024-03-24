package lu.kremi151.prefgen

import lu.kremi151.prefgen.extensions.android
import lu.kremi151.prefgen.extensions.variants
import lu.kremi151.prefgen.tasks.GenerateFragmentsTask
import lu.kremi151.prefgen.tasks.GeneratePrefRTask
import lu.kremi151.prefgen.tasks.ParsePreferenceXMLFiles
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
			val parserTaskName = "parsePrefXMLFilesForPrefgen${variant.name.capitalize()}"
			val genPrefRTaskName = "generatePrefRSrc${variant.name.capitalize()}"
			val genFragmentsTaskName = "generatePrefFragmentsSrc${variant.name.capitalize()}"

			val packageName = extension.packageName.get().ifBlank {
				variant.applicationId
			}

			val generatedRootDirPath = "${project.buildDir}/generated/prefgen"
			val parserOutputFile = File("${generatedRootDirPath}/parser/${variant.dirName}.csv")

			val parserTaskProvider = project.tasks.register(parserTaskName, ParsePreferenceXMLFiles::class.java) { parseTask ->
				parseTask.group = TASK_GROUP

				val xmlFiles = variant.sourceSets
					.flatMap { it.resDirectories }
					.map { File(it, "xml") }
					.filter { it.exists() && it.isDirectory }
					.flatMap { it.listFiles().toList() }
					.filter { it.isFile }

				parseTask.inputFiles = xmlFiles
				parseTask.parserOutputFile = parserOutputFile
			}

			val rootPrefRGenSrcPath = "${generatedRootDirPath}/prefr/${variant.dirName}"
			val prefROutputDir = File("$rootPrefRGenSrcPath/${packageName.replace(".", "/")}")
			val prefRTaskProvider = project.tasks.register(genPrefRTaskName, GeneratePrefRTask::class.java) { genTask ->
				genTask.group = TASK_GROUP

				genTask.dependsOn(parserTaskProvider)

				genTask.prefRFile = File(prefROutputDir, "PrefR.java")
				genTask.parserCsvFile = parserOutputFile
				genTask.packageName = packageName
			}
			variant.registerJavaGeneratingTask(prefRTaskProvider, File(rootPrefRGenSrcPath))

			if (extension.generateFragments.getOrElse(true)) {
				val fragmentsGenSrcPath = "${generatedRootDirPath}/fragments/${variant.dirName}"
				val fragmentsOutputDir = File("$fragmentsGenSrcPath/${packageName.replace(".", "/")}/fragments")
				val fragmentTaskProvider = project.tasks.register(genFragmentsTaskName, GenerateFragmentsTask::class.java) { genTask ->
					genTask.group = TASK_GROUP
					genTask.outputSourcesDir = fragmentsOutputDir

					genTask.dependsOn(parserTaskProvider)

					genTask.parserCsvFile = parserOutputFile
					genTask.packageName = "${packageName}.fragments"
				}
				variant.registerJavaGeneratingTask(fragmentTaskProvider, File(fragmentsGenSrcPath))
			}
		}
	}

}
package lu.kremi151.prefgen

import lu.kremi151.prefgen.util.PrefKeyAndType
import lu.kremi151.prefgen.util.PreferencesParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

internal open class AndroidPrefgenTask: DefaultTask() {

	@get:OutputFile
	lateinit var outputFile: File

	@get:Input
	lateinit var packageName: String

	@get:InputFiles
	lateinit var inputFiles: List<File>

	@TaskAction
	fun generateSources() {
		val keysAndPrefs = inputFiles.flatMap {
			try {
				PreferencesParser.tryParse(it)
			} catch (e: Exception) {
				logger.warn("Not a valid XML file: $it", e)
				null
			} ?: listOf()
		}

		BufferedWriter(FileWriter(outputFile)).use { writer ->
			generateJavaCode(writer, keysAndPrefs)
			writer.flush()
		}
	}

	private fun generateJavaCode(writer: BufferedWriter, keysAndPrefs: List<PrefKeyAndType>) {
		val uniqueKeys = keysAndPrefs.mapTo(HashSet()) { it.key }

		writer.appendLine("package $packageName;")
		writer.appendLine("public class PrefR {")
		uniqueKeys.forEach { key ->
			writer.appendLine("\tpublic static final String $key = \"${key}\";")
		}
		writer.appendLine("\tprivate PrefR() {}")
		writer.appendLine("}")
	}

}

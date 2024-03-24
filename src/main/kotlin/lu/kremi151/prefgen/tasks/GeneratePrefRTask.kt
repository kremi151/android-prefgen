package lu.kremi151.prefgen.tasks

import lu.kremi151.prefgen.util.PrefKeyAndType
import lu.kremi151.prefgen.util.PreferencesParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

internal open class GeneratePrefRTask: DefaultTask() {

	@get:OutputFile
	lateinit var prefRFile: File

	@get:Input
	lateinit var packageName: String

	@get:InputFile
	lateinit var parserCsvFile: File

	@TaskAction
	fun generateSources() {
		val keysAndPrefs = BufferedReader(FileReader(parserCsvFile)).use {
			PreferencesParser.readParserResult(it)
		}

		BufferedWriter(FileWriter(prefRFile)).use { writer ->
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

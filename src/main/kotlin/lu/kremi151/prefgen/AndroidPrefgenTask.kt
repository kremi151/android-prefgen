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
	lateinit var prefRFile: File

	@get:OutputFile
	var ktExtensionsFile: File? = null

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

		BufferedWriter(FileWriter(prefRFile)).use { writer ->
			generateJavaCode(writer, keysAndPrefs)
			writer.flush()
		}

		ktExtensionsFile?.let {
			BufferedWriter(FileWriter(it)).use { writer ->
				generateKotlinExtensions(writer, keysAndPrefs)
				writer.flush()
			}
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

	private fun generateKotlinExtensions(writer: BufferedWriter, keysAndPrefs: List<PrefKeyAndType>) {
		writer.appendLine("package $packageName")
		writer.appendLine("import androidx.preference.PreferenceFragmentCompat")

		val uniqueTypes = keysAndPrefs.mapTo(HashSet()) { it.type }
		uniqueTypes.forEach {
			writer.appendLine("import androidx.preference.$it")
		}

		val uniqueXmlFileNames = keysAndPrefs.mapTo(HashSet()) { it.xmlFileName }
		uniqueXmlFileNames.forEach { xmlFileName ->
			val javaifiedName = xmlFileName.stripXmlExtension().javaify()

			writer.appendLine("abstract class PreferenceFragment${javaifiedName}: PreferenceFragmentCompat()")
		}

		keysAndPrefs.forEach { pref ->
			val javaifiedName = pref.xmlFileName.stripXmlExtension().javaify()
			writer.appendLine("val PreferenceFragment${javaifiedName}.pref${pref.key.javaify()}: ${pref.type}? get() = findPreference<${pref.type}>(\"${pref.key}\")")
		}
	}

	private fun String.stripXmlExtension() = this.replace("\\.xml$".toRegex(), "")

	private fun String.javaify(): String = this
		.split("[^a-zA-Z]+".toRegex())
		.joinToString("") { it.capitalize() }

}

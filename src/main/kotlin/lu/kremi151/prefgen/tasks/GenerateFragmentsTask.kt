package lu.kremi151.prefgen.tasks

import lu.kremi151.prefgen.extensions.toJavaCamelCase
import lu.kremi151.prefgen.extensions.withoutXmlExtension
import lu.kremi151.prefgen.util.PrefKeyAndType
import lu.kremi151.prefgen.util.PreferencesParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.*

internal open class GenerateFragmentsTask: DefaultTask() {

    @get:OutputFile
    lateinit var outputSourcesDir: File

    @get:Input
    lateinit var packageName: String

    @get:InputFile
    lateinit var parserCsvFile: File

    @TaskAction
    fun generateSources() {
        checkNotNull(outputSourcesDir.listFiles()) { "Not a directory: $outputSourcesDir" }
            .forEach { it.deleteRecursively() }

        val keysAndPrefs = BufferedReader(FileReader(parserCsvFile)).use {
            PreferencesParser.readParserResult(it)
        }

        val fileToEntries = keysAndPrefs
            .groupBy { it.xmlFileName }
        fileToEntries.forEach { (xmlFileName, entries) ->
            val javaClassName = "PreferenceFragment${xmlFileName.withoutXmlExtension.toJavaCamelCase()}"
            val outputFile = File(outputSourcesDir, "${javaClassName}.java")
            BufferedWriter(FileWriter(outputFile)).use { writer ->
                generateFragment(writer, javaClassName, xmlFileName, entries)
                writer.flush()
            }
        }
    }

    private fun generateFragment(writer: BufferedWriter, className: String, xmlFileName: String, keysAndPrefs: List<PrefKeyAndType>) {
        writer.appendLine("package $packageName;")
        writer.appendLine("import android.os.Bundle;")
        writer.appendLine("import androidx.annotation.CallSuper;")
        writer.appendLine("import androidx.annotation.NonNull;")
        writer.appendLine("import androidx.annotation.Nullable;")
        writer.appendLine("import androidx.preference.PreferenceFragmentCompat;")

        val uniqueTypes = keysAndPrefs.mapTo(HashSet()) { it.type }
        uniqueTypes.forEach {
            writer.appendLine("import androidx.preference.$it;")
        }

        writer.appendLine("public class $className extends PreferenceFragmentCompat {")

        writer.appendLine("\t@Override")
        writer.appendLine("\t@CallSuper")
        writer.appendLine("\tpublic void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {")
        writer.appendLine("\t\tsetPreferencesFromResource(R.xml.${xmlFileName.withoutXmlExtension}, rootKey);")
        writer.appendLine("\t")

        keysAndPrefs.forEach { pref ->
            writer.appendLine("\t@NonNull")
            writer.appendLine("\tprotected final ${pref.type} get${pref.key.toJavaCamelCase()}Preference() {")
            writer.appendLine("\t\treturn this.<${pref.type}>findPreference(\"${pref.key}\");")
            writer.appendLine("\t}")
        }

        writer.appendLine("}")
    }

}
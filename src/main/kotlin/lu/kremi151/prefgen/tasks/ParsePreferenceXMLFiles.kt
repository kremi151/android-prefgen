package lu.kremi151.prefgen.tasks

import lu.kremi151.prefgen.util.PreferencesParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

internal open class ParsePreferenceXMLFiles: DefaultTask() {

    @get:InputFiles
    lateinit var inputFiles: List<File>

    @get:OutputFile
    lateinit var parserOutputFile: File

    @TaskAction
    fun parseXMLFiles() {
        val keysAndPrefs = inputFiles.flatMap {
            try {
                PreferencesParser.tryParse(it)
            } catch (e: Exception) {
                logger.warn("Not a valid XML file: $it", e)
                null
            } ?: listOf()
        }
        BufferedWriter(FileWriter(parserOutputFile)).use {
            PreferencesParser.writeParserResult(keysAndPrefs, it)
        }
    }
}
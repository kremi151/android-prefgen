package lu.kremi151.prefgen.extensions

val String.withoutXmlExtension get() = this.replace("\\.xml$".toRegex(), "")

fun String.toJavaCamelCase(): String = this
    .split("[^a-zA-Z]+".toRegex())
    .joinToString("") { it.capitalize() }

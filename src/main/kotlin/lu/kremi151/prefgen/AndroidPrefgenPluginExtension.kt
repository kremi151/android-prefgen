package lu.kremi151.prefgen

import org.gradle.api.provider.Property

abstract class AndroidPrefgenPluginExtension {
	abstract val packageName: Property<String>
	abstract val generateKotlinExtensions: Property<Boolean>

	init {
		packageName.convention("")
		generateKotlinExtensions.convention(false)
	}
}
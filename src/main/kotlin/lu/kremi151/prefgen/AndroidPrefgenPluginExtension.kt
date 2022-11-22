package lu.kremi151.prefgen

import org.gradle.api.provider.Property

abstract class AndroidPrefgenPluginExtension {
	abstract val packageName: Property<String>

	init {
		packageName.convention("")
	}
}
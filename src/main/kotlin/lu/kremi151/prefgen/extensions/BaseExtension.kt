package lu.kremi151.prefgen.extensions

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException

val BaseExtension.variants: DomainObjectSet<out BaseVariant>
	get() = when (this) {
		is AppExtension -> applicationVariants
		is LibraryExtension -> libraryVariants
		else -> throw GradleException("Unsupported BaseExtension type!")
	}

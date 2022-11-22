package lu.kremi151.prefgen.extensions

import com.android.build.gradle.BaseExtension
import org.gradle.api.GradleException
import org.gradle.api.Project

val Project.android: BaseExtension
	get() = project.extensions.findByType(BaseExtension::class.java)
		?: throw GradleException("Project $name is not an Android project")

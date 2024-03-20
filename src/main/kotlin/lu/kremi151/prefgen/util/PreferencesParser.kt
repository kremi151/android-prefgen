package lu.kremi151.prefgen.util

import lu.kremi151.prefgen.extensions.toList
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory

object PreferencesParser {

	private const val TAG_PREFERENCE_SCREEN = "androidx.preference.PreferenceScreen"
	private const val TAG_PREFERENCE_CATEGORY = "PreferenceCategory"

	private const val TAG_CHECKBOX_PREFERENCE = "CheckBoxPreference"
	private const val TAG_DIALOG_PREFERENCE = "DialogPreference"
	private const val TAG_DROPDOWN_PREFERENCE = "DropDownPreference"
	private const val TAG_EDIT_TEXT_PREFERENCE = "EditTextPreference"
	private const val TAG_LIST_PREFERENCE = "ListPreference"
	private const val TAG_MULTI_SELECT_PREFERENCE = "MultiSelectListPreference"
	private const val TAG_PREFERENCE = "Preference"
	private const val TAG_SEEK_BAR_PREFERENCE = "SeekBarPreference"
	private const val TAG_SWITCH_PREFERENCE = "SwitchPreference"
	private const val TAG_TWO_STATE_PREFERENCE = "TwoStatePreference"

	private const val NS_RES_AUTO = "http://schemas.android.com/apk/res-auto"

	private const val ATTR_KEY = "key"

	private val preferenceTags = setOf(
		TAG_CHECKBOX_PREFERENCE,
		TAG_DIALOG_PREFERENCE,
		TAG_DROPDOWN_PREFERENCE,
		TAG_EDIT_TEXT_PREFERENCE,
		TAG_LIST_PREFERENCE,
		TAG_MULTI_SELECT_PREFERENCE,
		TAG_PREFERENCE,
		TAG_SEEK_BAR_PREFERENCE,
		TAG_SWITCH_PREFERENCE,
		TAG_TWO_STATE_PREFERENCE,
	)

	fun tryParse(file: File): List<PrefKeyAndType>? {
		val builderFactory = DocumentBuilderFactory.newInstance().apply {
			isNamespaceAware = true
		}
		val docBuilder = builderFactory.newDocumentBuilder()
		val doc = FileInputStream(file).use { docBuilder.parse(it) }

		val keysAndPrefs = mutableListOf<PrefKeyAndType>()

		val childNodes = doc.childNodes
		for (i in 0 until childNodes.length) {
			val childNode = childNodes.item(i)
			if (TAG_PREFERENCE_SCREEN == childNode.nodeName && childNode is Element) {
				parseRoot(childNode, file, keysAndPrefs)
			}
		}

		return keysAndPrefs
	}

	private fun parsePreference(element: Element, file: File, outKeys: MutableList<PrefKeyAndType>) {
		val key: String? = element.getAttributeNS(NS_RES_AUTO, ATTR_KEY)
		if (key?.isNotBlank() == true) {
			outKeys.add(PrefKeyAndType(
				key = key,
				type = element.nodeName,
				xmlFileName = file.name,
			))
		}
	}

	private fun parseRoot(root: Element, file: File, outKeys: MutableList<PrefKeyAndType>) {
		val childNodes = preferenceTags.flatMap {
			root.getElementsByTagName(it).toList()
		}
		for (node in childNodes) {
			if (node is Element) {
				parsePreference(node, file, outKeys)
			} else {
				println("Not an element: ${node.nodeName}")
			}
		}
	}

}
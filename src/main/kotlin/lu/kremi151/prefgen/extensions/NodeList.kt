package lu.kremi151.prefgen.extensions

import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun NodeList.toList(): List<Node> {
	val list = mutableListOf<Node>()
	for (i in 0 until length) {
		list.add(item(i))
	}
	return list
}

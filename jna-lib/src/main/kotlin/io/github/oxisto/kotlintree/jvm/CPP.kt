package io.github.oxisto.kotlintree.jvm

import java.io.File

class CppParser : Parser() {

    val symbols = mutableMapOf<String, String>()

    fun parse(file: File): Tree {
        this.language = TreeSitterCpp.INSTANCE.tree_sitter_cpp()

        val input = Input(file.readText())

        val tree = this.parseString(null, input.string)

        return preprocess(tree, input, file)
    }

    fun getCode(node: Node, input: String): String {
        val start = node.startByte
        val end = node.endByte

        return input.substring(start, end)
    }

    class Input(var string: String) {}

    private fun preprocess(tree: Tree, input: Input, file: File): Tree {
        var t = tree
        var s = input

        var changed = false

        // TODO: replace with query
        var root = t.rootNode

        println("pre-tree: ${t.rootNode.string}")

        var it = root.iterator()

        while (it.hasNext()) {
            var child = it.next()
            if (child.type == "preproc_include") {
                val pathNode = "path" of child
                // TODO: differentiate between "test.h" and <test.h>
                val path = getCode(pathNode, input.string).replace("\"", "")

                println("Need to include $path relative to ${file.parent}")
                val header = file.parentFile.resolve(File(path))
                if (header.exists()) {
                    var headerInput = header.readText()

                    // make sure, that headerInput ends with a new line
                    if (headerInput.last() != '\n') {
                        headerInput = "$headerInput\n"
                    }

                    // one more line to compensate for the #include line
                    headerInput = "$headerInput\n"

                    editTree(input, child, headerInput, t)
                    changed = true
                }
            } else if (child.type == "preproc_def") {
                val nameNode = "name" of child
                val valueNode = "value" of child

                val name = getCode(nameNode, input.string)
                val value = getCode(valueNode, input.string)

                println("Adding $name=$value")

                // add it to the symbols list
                symbols[name] = value
            } else {
                changed = replaceIdentifiers(t, child, input)
            }

            if (changed) {
                // we should do this inside the for-loop, which is probably not that easy so we can
                // it
                // recursively
                println("Re-parsing")
                t = this.parseString(t, input.string)
                changed = false

                // update the iterator (this re-parses the file several times and is slow and needs
                // to be improved)
                it = t.rootNode.iterator()
            }
        }

        println("post-tree: ${t.rootNode.string}")

        return t
    }

    private fun replaceIdentifiers(tree: Tree, node: Node, input: Input): Boolean {
        var changed = false

        for (i in 0 until node.namedChildCount) {
            val child = i ofNamed node
            if (child.type == "identifier") {
                val identifier = getCode(child, input.string)

                symbols[identifier]?.let { value ->
                    println("Replacing $identifier with $value")

                    editTree(input, child, value, tree)

                    changed = true
                }
            } else {
                if (replaceIdentifiers(tree, child, input)) {
                    changed = true
                }
            }
        }

        return changed
    }

    private fun editTree(input: Input, child: Node, value: String, tree: Tree) {
        // TODO: this creates a lot of extra strings, so we need to make this somehow mutable
        input.string = input.string.replaceRange(child.startByte, child.endByte, value)

        val length = child.endByte - child.startByte

        val inputEdit = InputEdit()
        inputEdit.start_byte = child.startByte
        inputEdit.old_end_byte = child.endByte
        inputEdit.new_end_byte = child.endByte + value.length - (length)

        // TODO: also adjust points

        inputEdit.write()

        tree.edit(inputEdit)
    }
}

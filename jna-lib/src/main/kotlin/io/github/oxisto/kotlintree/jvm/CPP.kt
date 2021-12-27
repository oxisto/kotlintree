package io.github.oxisto.kotlintree.jvm

import java.io.File

class CppParser : Parser() {

    val symbols = mutableMapOf<String, String>()

    fun parse(file: File): Tree {
        this.language = TreeSitterCpp.INSTANCE.tree_sitter_cpp()

        val input = Input(file.readText())

        var tree = this.parseString(null, input.string)

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

        // TODO: replace with query
        val root = tree.rootNode

        println("pre-tree: ${t.rootNode.string}")

        for (child in root) {
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

                    editTree(input, child, headerInput, tree)
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
                replaceIdentifiers(tree, child, input)
            }
        }

        // we should do this inside the for-loop, which is probably not that easy so we can it
        // recursively
        t = this.parseString(tree, input.string)

        println("post-tree: ${t.rootNode.string}")

        return t
    }

    private fun replaceIdentifiers(tree: Tree, node: Node, input: Input) {
        for (i in 0 until node.namedChildCount) {
            val child = i ofNamed node
            if (child.type == "identifier") {
                val identifier = getCode(child, input.string)

                symbols[identifier]?.let { value ->
                    println("Replacing $identifier with $value")

                    editTree(input, child, value, tree)
                }
            } else {
                replaceIdentifiers(tree, child, input)
            }
        }
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

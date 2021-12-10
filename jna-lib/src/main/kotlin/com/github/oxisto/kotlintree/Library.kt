package com.github.oxisto.kotlintree

import com.sun.jna.*
import com.sun.jna.Structure.ByValue

class Language : Structure, Structure.ByReference {
    @JvmField var version: Int = 0
    @JvmField var symbol_count: Int = 0

    constructor() : super()
    constructor(pointer: Pointer) : super(pointer)

    fun symbolName(symbol: Short): String {
        return TreeSitter.INSTANCE.ts_language_symbol_name(this, symbol)
    }

    override fun getFieldOrder() = listOf(
        "version", "symbol_count"
    )
}

/**
 * Represents `TSNode`. Note, that since `TSNode` is a structure, functions that return a [Node] such as [Tree.rootNode]
 * will always return a (potentially) empty node structure rather than `null`. In the C API, the function [TreeSitter.ts_node_is_null]
 * is needed to check before retrieving more information, e.g. by using [TreeSitter.ts_node_string].
 *
 * For convenience and extra safety, calls to the properties such as [string] will therefore internally check for [isNull]
 * before further interacting with a node.
 */
class Node : Structure(), ByValue {
    @JvmField var context = intArrayOf(0, 0, 0, 0)
    @JvmField var id: Pointer? = null
    @JvmField var tree: Tree? = null

    val string: String?
    get() {
        return if(!isNull) { TreeSitter.INSTANCE.ts_node_string(this) } else { null }
    }

    val type: String?
    get() {
        return if(!isNull) { TreeSitter.INSTANCE.ts_node_string(this) } else { null }
    }

    val childCount: Int
    get() {
        return if(!isNull) { TreeSitter.INSTANCE.ts_node_child_count(this) } else { 0 }
    }

    val namedChildCount: Int
        get() {
            return if(!isNull) {  TreeSitter.INSTANCE.ts_node_named_child_count(this) }else { 0 }
        }

    val isNull: Boolean
    get() {
        // instead of calling ts_node_is_null we avoid the extra JNA round-trip and directly check, whether the id field is null (which is exactly what ts_node_is_null does)
        return id == null
    }

    public override fun getFieldOrder(): List<String> {
        return listOf("context", "id", "tree")
    }

    fun namedChild(index: Int): Node {
        return TreeSitter.INSTANCE.ts_node_named_child(this, index)
    }
}

open class Length : Structure(), Structure.ByValue {
    @JvmField var bytes: Int = 0
    @JvmField var extent: Point = Point()

    override fun getFieldOrder() = listOf(
        "bytes", "extent"
    )
}

open class Point : Structure(), Structure.ByValue {
    @JvmField var row: Int = 0
    @JvmField var column: Int = 0

    override fun getFieldOrder() = listOf(
        "row", "column"
    )
}

class Logger : Structure(), Structure.ByValue {
    @JvmField var payload: Pointer? = null
    @JvmField var log: LogCallback? = null

    override fun getFieldOrder() = listOf(
        "payload", "log"
    )
}

interface LogCallback : Callback {
    fun log(payload: Pointer?, type: Int, msg: String)
}

class Tree : PointerType() {
    val language: Language
    get() {
        return TreeSitter.INSTANCE.ts_tree_language(this)
    }

    val rootNode: Node
    get() {
        return TreeSitter.INSTANCE.ts_tree_root_node(this)
    }
}

class Parser : PointerType(TreeSitter.INSTANCE.ts_parser_new()) {

    var language: Language?
        set(language) {
        if(language != null) {
            TreeSitter.INSTANCE.ts_parser_set_language(this, language)
        }
    }
    get(): Language? {
        return TreeSitter.INSTANCE.ts_parser_language(this)
    }

    fun parseString(oldTree: Tree?, string: String): Tree {
        return TreeSitter.INSTANCE.ts_parser_parse_string(this, oldTree, string.toByteArray(), string.length)
    }
}

interface TreeSitter : Library {
    /**
     * Creates a new `TSParser`. Note, this intentionally returns a [Pointer] instead of [Parser] because we execute this function in the constructor of [Parser].
     */
    fun ts_parser_new(): Pointer
    fun ts_parser_set_language(self: Parser, language: Language)
    fun ts_parser_language(self: Parser): Language
    fun ts_parser_set_logger(self: Parser, logger: Logger)

    fun ts_parser_delete(parser: Parser)
    fun ts_parser_parse_string(self: Parser, oldTree: Tree?, string: ByteArray, length: Int): Tree
    fun ts_tree_root_node(self: Tree): Node

    fun ts_node_new(tree: Tree?, subtree: Pointer?, position: Structure, alias: Int): Node
    fun ts_tree_language(self: Tree): Language
    fun ts_node_start_byte(node: Node): Int
    fun ts_node_string(node: Node): String
    fun ts_node_type(node: Node): String
    fun ts_node_child_count(node: Node): Int
    fun ts_node_named_child_count(node: Node): Int
    fun ts_node_named_child(node: Node, childIndex: Int): Node
    fun ts_node_is_null(node: Node): Boolean

    fun ts_language_symbol_name(language: Language, symbol: Short): String

    companion object {
        val INSTANCE = Native.load("tree-sitter",
            TreeSitter::class.java
        ) as TreeSitter
    }
}

interface TreeSitterCpp : Library {
    fun tree_sitter_cpp(): Language

    companion object {
        val INSTANCE = Native.load("tree-sitter-cpp",
            TreeSitterCpp::class.java
        ) as TreeSitterCpp
    }

}

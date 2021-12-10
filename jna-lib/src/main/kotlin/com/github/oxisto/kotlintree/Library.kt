package com.github.oxisto.kotlintree

import com.sun.jna.*
import com.sun.jna.Structure.ByValue


interface CLibrary : Library {
    fun printf(format: String?, vararg args: Any?)

    companion object {
        val INSTANCE = Native.load(
            if (Platform.isWindows()) "msvcrt" else "c",
            CLibrary::class.java
        ) as CLibrary
    }
}

/*open class Node: Structure(), Structure.ByValue {
    @JvmField var context: Array<Int> = arrayOf(0,0,0,0)
    @JvmField var id: Pointer? = null
    @JvmField var tree: Tree? = null

    override fun getFieldOrder() = listOf(
        "context", "id", "tree"
    )
}*/

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

class Node : Structure(), ByValue {
    @JvmField var context = intArrayOf(0, 0, 0, 0)
    @JvmField var id: Pointer? = null
    @JvmField var tree: Tree? = null

    val string: String
    get() {
        return TreeSitter.INSTANCE.ts_node_string(this)
    }

    val type: String
    get() {
        return TreeSitter.INSTANCE.ts_node_type(this)
    }

    val childCount: Int
    get() {
        return TreeSitter.INSTANCE.ts_node_child_count(this)
    }

    val namedChildCount: Int
        get() {
            return TreeSitter.INSTANCE.ts_node_named_child_count(this)
        }

    val isNull: Boolean
    get() {
        return TreeSitter.INSTANCE.ts_node_is_null(this)
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

    /*protected fun finalize() {
        TreeSitter.INSTANCE.ts_parser_delete(this)
    }*/

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

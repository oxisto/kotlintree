package com.github.oxisto.kotlintree

import com.sun.jna.*


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
    //   void (*log)(void *payload, TSLogType, const char *);
    @JvmField var log: LogFunc? = null

    override fun getFieldOrder() = listOf(
        "payload", "log"
    )
}

interface LogFunc : Callback {
    fun log(payload: Pointer?, type: Int, msg: String)
}

class Tree : PointerType() {
    val language: Language
    get() {
        return TreeSitter.INSTANCE.ts_tree_language(this)
    }

    val rootNode: Node
    get() {
        TreeSitter.INSTANCE.ts_tree_root_node(this)
        /*return  Node()
        TreeSitter.INSTANCE.ts_node_new(this, null, Length(), 0)
        return  Node()*/
        return Node()
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

    protected fun finalize() {
        TreeSitter.INSTANCE.ts_parser_delete(this)
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
    fun ts_tree_root_node(self: Tree?): Node

    fun ts_node_new(tree: Tree?, subtree: Pointer?, position: Structure, alias: Int): Node
    fun ts_tree_language(self: Tree): Language
    fun ts_node_start_byte(node: Node): Int

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

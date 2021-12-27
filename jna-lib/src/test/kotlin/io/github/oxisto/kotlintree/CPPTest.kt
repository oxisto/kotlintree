package io.github.oxisto.kotlintree

import io.github.oxisto.kotlintree.jvm.CppParser
import java.io.File
import kotlin.test.assertEquals
import org.junit.Test

class CPPTest {
    @Test
    fun testPreprocess() {
        val parser = CppParser()
        val file = File("src/test/resources/test.cpp")

        val tree = parser.parse(file)

        val root = tree.rootNode

        val s =
            "(translation_unit (function_definition type: (primitive_type) declarator: (function_declarator declarator: (identifier) parameters: (parameter_list)) body: (compound_statement)) (preproc_def name: (identifier) value: (preproc_arg)) (preproc_def name: (identifier) value: (preproc_arg)) (function_definition type: (primitive_type) declarator: (function_declarator declarator: (identifier) parameters: (parameter_list)) body: (compound_statement (return_statement (binary_expression left: (number_literal) right: (number_literal))))))"

        assertEquals(s, root.string)
    }
}

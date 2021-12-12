package io.github.oxisto.kotlintree

import io.github.oxisto.kotlintree.jvm.Parser
import io.github.oxisto.kotlintree.jvm.TreeSitterCpp
import io.github.oxisto.kotlintree.jvm.of
import kotlin.test.*

class CursorTest {
    @Test
    fun testIterator() {
        Parser().use {
            it.language = TreeSitterCpp.INSTANCE.tree_sitter_cpp()

            val source = "int main(int argc, char** argv) {}"
            val tree = it.parseString(null, source)
            val root = tree.rootNode
            val func = 0 of root

            val cursor = func.iterator()
            assertNotNull(cursor)

            assertTrue(cursor.hasNext())
            var next = cursor.next()
            assertNotNull(next)
            assertFalse(next.isNull)
            assertEquals("primitive_type", next.type)
            assertEquals("type", cursor.currentFieldName)

            next = cursor.next()
            assertNotNull(next)
            assertFalse(next.isNull)
            assertEquals("function_declarator", next.type)
            assertEquals("declarator", cursor.currentFieldName)

            next = cursor.next()
            assertNotNull(next)
            assertFalse(next.isNull)
            assertEquals("compound_statement", next.type)
            assertEquals("body", cursor.currentFieldName)
        }
    }

    @Test
    fun testList() {
        Parser().use {
            it.language = TreeSitterCpp.INSTANCE.tree_sitter_cpp()

            val source = "int main(int argc, char** argv) {}"
            val tree = it.parseString(null, source)
            val root = tree.rootNode
            val func = 0 of root

            val children = func.toList()
            assertEquals(3, children.size)
        }
    }
}

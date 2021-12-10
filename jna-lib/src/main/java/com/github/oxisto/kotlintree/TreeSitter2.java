package com.github.oxisto.kotlintree;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

public interface TreeSitter2 extends Library {
    TreeSitter2 INSTANCE = (TreeSitter2)
            Native.load("tree-sitter",
    TreeSitter2.class);

    Pointer ts_tree_root_node(Pointer self);
}

package com.github.oxisto.kotlintree;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.List;

public class Node extends Structure {

    public int[] context = {0,0,0,0};
    public Pointer id;
    public Tree tree;

    public class ByValue implements Structure.ByValue {

    }

    public List<String> getFieldOrder() {
        return List.of("context", "id", "tree");
    }
}

#!/usr/bin/env bash

ARCH=`uname -s`
TREE_SITTER_VERSION=0.20.1
TREE_SITTER_CPP_VERSION=0.19.0
wget https://github.com/tree-sitter/tree-sitter/archive/refs/tags/v$TREE_SITTER_VERSION.tar.gz -O tree-sitter-$TREE_SITTER_VERSION.tar.gz
wget https://github.com/tree-sitter/tree-sitter-cpp/archive/refs/tags/v$TREE_SITTER_CPP_VERSION.tar.gz -O tree-sitter-cpp-$TREE_SITTER_CPP_VERSION.tar.gz

tar -xzvf tree-sitter-$TREE_SITTER_VERSION.tar.gz
tar -xzvf tree-sitter-cpp-$TREE_SITTER_CPP_VERSION.tar.gz

export LIBDIR=`pwd`/src/main/resources
export INSTALL_PATH=`pwd`/src/main/resources

cd tree-sitter-$TREE_SITTER_VERSION

if [ $ARCH == "Darwin" ]; then
        export PREFIX=$INSTALL_PATH
        export LDFLAGS="-Wl,-rpath,@loader_path/"
        make clean
        make
        make install
        cd ../tree-sitter-cpp-0.19.0/src
        export PREFIX=$INSTALL_PATH
        export SONAME_MAJOR=0
        export SONAME_MINOR=0
        export SOEXT=dylib
	      export SOEXTVER_MAJOR=$SONAME_MAJOR.dylib
	      export SOEXTVER=$SONAME_MAJOR.$SONAME_MINOR.dylib
        clang++ $CFLAGS $LDFLAGS -I. scanner.cc parser.c -dynamiclib -Wl,-install_name,$LIBDIR/libtree-sitter-cpp.$SONAME_MAJOR.dylib -o libtree-sitter-cpp.$SOEXTVER
        cp libtree-sitter-cpp.$SOEXTVER $LIBDIR
        cd $LIBDIR
        ln -sf libtree-sitter-cpp.$SOEXTVER libtree-sitter-cpp.$SOEXT
	    ln -sf libtree-sitter-cpp.$SOEXTVER libtree-sitter-cpp.$SOEXTVER_MAJOR
        #cp ../../../cpp.h ../include/tree_sitter
fi

if [ $ARCH == "Linux" ]; then
        export CFLAGS="-m64 -fPIC"
          export PREFIX=$INSTALL_PATH
          make clean
                  make
                  make install
        cd ../tree-sitter-cpp-0.19.0/src
          export SONAME_MAJOR=0
          export SONAME_MINOR=0
          export SOEXT=so
  	    export SOEXTVER_MAJOR=so.$SONAME_MAJOR
  	    export SOEXTVER=so.$SONAME_MAJOR.$SONAME_MINOR
          clang++ $CFLAGS $LDFLAGS -I. scanner.cc parser.c -shared -Wl,-soname,$LIBDIR/libtree-sitter-cpp.so.$SONAME_MAJOR -o libtree-sitter-cpp.$SOEXTVER
          cp libtree-sitter-cpp.$SOEXTVER $LIBDIR
          cd $LIBDIR
          ln -sf libtree-sitter-cpp.$SOEXTVER libtree-sitter-cpp.$SOEXT
  	    ln -sf libtree-sitter-cpp.$SOEXTVER libtree-sitter-cpp.$SOEXTVER_MAJOR
fi

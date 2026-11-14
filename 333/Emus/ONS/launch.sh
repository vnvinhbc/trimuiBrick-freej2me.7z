#!/bin/sh
echo $0 $*
mydir=`dirname "$0"`
gamedir=`dirname "$1"`

export HOME=$gamedir
export PATH=$mydir:$PATH
export LD_LIBRARY_PATH=$mydir/lib:$LD_LIBRARY_PATH


cd $gamedir
onscripter



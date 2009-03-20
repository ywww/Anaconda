#!/bin/bash
# This scripts is used to find Scilab on a remote host.
# it prints out only the path to the root directory of Scilab

cmd_line=
bin_dir=
scilab_home=
lib_dir=
m1=`which scilab 2>/dev/null|grep -v alias`
if [ "$m1" != "" ]; then
    cmd_line=`readlink -f $m1`
    bin_dir=`dirname $cmd_line`
    old=`pwd`
    cd $bin_dir/..
    scilab_home=`pwd`
    lib_dir=`dirname $(find . -name 'libjavasci.so')|awk '{print substr($0,3)}'`
    cd $old
     
fi
if [ "$scilab_home" != "" ]; then
    echo $scilab_home
    echo $lib_dir
    echo $scilab_home/share/scilab
else
    host=`hostname`
    echo "Scilab dir not found on $host"
    exit 1
fi
echo
#!/bin/bash

PATH=$PATH:/Applications/ChemAxon/JChem-5.7.0/bin

# rm ReactorDemo.class
# javac -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar -Xlint:unchecked ReactorDemo.java
# java -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar ReactorDemo $1 $2 $3 #| tee /dev/stderr | mview - &

#rm RetroTests.class
#javac -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar -Xlint:unchecked RetroTests.java
#java -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar RetroTests $1 $2 $3 #| tee /dev/stderr | mview - &

java -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar chemaxon.reaction.Reactor $* #| tee /dev/stderr | mview - &

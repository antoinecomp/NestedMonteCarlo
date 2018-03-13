#!/bin/bash

PATH=$PATH:/Applications/ChemAxon/JChem/bin

# rm ReactorDemo.class
# javac -cp .:/Applications/ChemAxon/JChem/lib/jchem.jar -Xlint:unchecked ReactorDemo.java
# java -cp .:/Applications/ChemAxon/JChem/lib/jchem.jar ReactorDemo $1 $2 $3 #| tee /dev/stderr | mview - &
name=ReactionReader
rm $name.class
javac -cp .:/Applications/ChemAxon/JChem/lib/jchem.jar -Xlint:unchecked $name.java
java -cp .:/Applications/ChemAxon/JChem/lib/jchem.jar $name $1 #| tee /dev/stderr | mview - &

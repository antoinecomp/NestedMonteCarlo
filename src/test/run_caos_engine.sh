#!/bin/bash

JCHEM=/Applications/ChemAxon/JChem-5.7.0
MAXMEM="2G"
if [[ `uname -a | grep Linux` ]] ;
then
JCHEM=/gpfs/home/aheifets/opt/jchem-5.7.1
MAXMEM="8G"
fi

TOP=`cd $(dirname $0)/../..; pwd -P`
PATH=$PATH:${JCHEM}/bin

# rm ReactorDemo.class
# javac -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar -Xlint:unchecked ReactorDemo.java
# java -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar ReactorDemo $1 $2 $3 #| tee /dev/stderr | mview - &

#rm RetroTests.class
#javac -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar -Xlint:unchecked RetroTests.java
#java -cp .:/Applications/ChemAxon/JChem-5.7.0/lib/jchem.jar RetroTests $1 $2 $3 #| tee /dev/stderr | mview - &

date
#java -Xmx2G -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/cleaned_reaction_library_JChem_5.7.0_DISABLED  ${TOP}/data/SML ${TOP}/data/33.smiles 8000 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML

#java -Xmx2G -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/BENCHMARK/PROBLEMS/3/RXNs  ${TOP}/data/BENCHMARK/PROBLEMS/3/SML ${TOP}/data/BENCHMARK/PROBLEMS/3/goal_d3.smarts 1 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML

PROBLEM=16
#for PROBLEM in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 18.1 19 20
for PROBLEM in 11  #8 10 11 12 #1 12 14 15 16 #15 #11 12 14 16 18 19 8 9
#for PROBLEM in 20
do
OUTDIR=rxndebug #exhaust #
mkdir -p ${TOP}/out/${OUTDIR}/${PROBLEM}
LOG=${TOP}/out/${OUTDIR}/${PROBLEM}/log
ERR=${TOP}/out/${OUTDIR}/${PROBLEM}/err

echo "==================================PROBLEM ${PROBLEM}=================================="
echo "==================================PROBLEM ${PROBLEM}==================================" > ${LOG}
echo "==================================PROBLEM ${PROBLEM}==================================" > ${ERR}
date
date > ${ERR}
#java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar test.ReactorDemo2 $*
#java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.OperatorLibrary

#java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/RXNs  ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/SML ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/goal.smiles 10000 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs_DISABLED ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML >${LOG} 2>${ERR}

java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/RXNs_DISABLED  ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/SML ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/goal.smiles 10000 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML >${LOG} 2>${ERR}



#java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/RXNs  ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/SML ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/goal.smiles 5 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML EXHAUSTIVE >${LOG} 2>>${ERR} 

#java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/RXNs  ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/SML ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/subgoals/goal_c.smiles 100 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML
#java -Xmx${MAXMEM} -cp .:${TOP}/build:${JCHEM}/lib/jchem.jar caos.aaai.CaosEngine ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/RXNs  ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/SML ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/subgoals/solution_B.smiles 100 0 ${TOP}/data/BENCHMARK/PROBLEMS/GlobalRXNs ${TOP}/data/BENCHMARK/PROBLEMS/GlobalSML
diff -b $LOG ${TOP}/data/BENCHMARK/PROBLEMS/${PROBLEM}/SOLUTION.txt

date
date >> ${ERR}
done
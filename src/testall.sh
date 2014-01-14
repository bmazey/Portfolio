#!/bin/bash
cd test-cases/

FILES=*

for f in $FILES
do
if [[ "$f" == Test*.java ]]
then
fname="${f%.*}"
echo "Testing " $fname
javac $f
java $fname > $fname.out
javac Translator.java
java Translator -translateToCPP $f > tmp.out
g++ $fname.cpp -o $fname.exe
./$fname.exe > $fname.cpp.out
diff $fname.out $fname.cpp.out
rm *.out
rm *.class
rm *.cpp
rm *.exe
fi
done

echo "Testing complete."

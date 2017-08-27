#/bin/sh
x=0
while [ "$x" -lt 100 -a ! -e "updater.jar" ]; do
    x=$((x+1))
    sleep .1
done
if [ -e $FILE ]
then
    echo "Found: updater.jar"
    rm "updater.jar"
    rm -- "$0"
else
    echo "File $FILE not found within time limit!"
fi
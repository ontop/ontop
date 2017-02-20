#!/bin/bash
# Usage: move all datasource information from the .obda file in a new file. 
# Take as argument the directory with the obda file to modify (it searches also in the subdirectories)
# Example sh datasource_move.sh test


dir="$1"
# for f in $@
if [ -d "$dir" ]
then
	for f in $(find $dir -type f -name '*.obda')
        do

            if grep -q "^\[SourceDeclaration\]" $f
            then
                :
            else
                echo "No datasource in obda file - $f"
                continue
            fi

                echo "Moving datasource from obda file - $f"
                rm -f ${f%.*}.properties

            # 	copy in a new file all 5 lines
                # 	cat $f | grep -A 4 "^\[SourceDeclaration\]" > ${f%.*}.properties
                #   grep -A 4 "^\[SourceDeclaration\]" $f  > ${f%.*}.properties
                # 	awk '/^\[SourceDeclaration\]/{c=4}c&&c--' $f > ${f%.*}.properties

            # 	copy a certain block
                # 	awk '/connectionUrl/,/username/'  $f >>   ${f%.*}.properties

            # 	copy one line at a time
                awk '/^sourceUri/{print "jdbc.name = "$2}'  $f >>   ${f%.*}.properties
                awk '/^connectionUrl/{print "jdbc.url = "$2}'  $f >>   ${f%.*}.properties
                awk '/^username/{print "jdbc.user = "$2}'  $f >>   ${f%.*}.properties
                awk '/^password/{print "jdbc.password = "$2}'  $f >>   ${f%.*}.properties
                awk '/^driverClass/{print "jdbc.driver = "$2}'  $f >>   ${f%.*}.properties

            # 	remove from file all 5 lines
                #	sed -i -e '/^\[SourceDeclaration\]/{N;N;N;N;N;d;}'  $f
                sed -i '' -e  '/^\[SourceDeclaration\]/{N;N;N;N;N;d;}'  $f

        done
else
	echo "Need as argument the directory with the obda file to modify (it searches also in the subdirectories)"
fi
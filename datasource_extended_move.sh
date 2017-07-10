#!/bin/bash
# Usage: move all datasource information from the .obda file in a new file .properties.
# Take as argument the directory with the obda file to modify (it searches also in the subdirectories)
# Example sh datasource_extended_move.sh test

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

            # 	condition for the old syntax
                if grep -q '<dataSource' $f
                then
                    echo "Old format obda file - $f"
                    rm -f ${f%.*}.properties

                # 	get old syntax parameter and print in new properties file
                    awk 'BEGIN { RS="<dataSource "; FS="\" |\"\t|\"\n|=\"";  }
                        /URI/{
                        print "jdbc.name = "$2 }
                        /databaseURL/{ print "jdbc.url = "$8; }
                        /databaseUsername/{ print "jdbc.user = "$10; }
                        /databasePassword/{print "jdbc.password = "$6}
                        /databaseDriver/{ print "jdbc.driver = "$4; }' $f >>   ${f%.*}.properties

                 # 	remove from old syntax obda file the datasource part
                    sed -i '' -e '1h;2,$H;$!d;g' -e 's/<dataSource.*\/>//g' $f

                else
                    echo "No datasource in obda file - $f"
                    continue
                fi
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

            # 	copy one line at a time support multiple datasources
                awk 'BEGIN { RS="\[SourceDeclaration\]"; FS="\t| |\n";  }
                /sourceUri/{print "jdbc.name = "$3 }
                /connectionUrl/{print "jdbc.url = "$5 }
                /username/{print "jdbc.user = "$7 }
                /password/{print "jdbc.password = "$9}
                /driverClass/{print "jdbc.driver = "$11 }'  $f >>   ${f%.*}.properties

            # 	remove from obda file all 5 lines even in case of multiple datasources
                sed -i '' -e  '/^\[SourceDeclaration\]/{N;N;N;N;N;d;}'  $f

        done
else
	echo "Need as argument the directory with the obda file to modify (it searches also in the subdirectories)"
fi

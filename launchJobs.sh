#!/bin/bash

# premier paramètre facultatif : la date JJ/MM/AAAA, par défaut la date courante
if [ -n "$1" ]
then
  date=$1
else
  date=$(date +"%d/%m/%Y")
fi

echo $date

dir=target
vmOptions="-DDATE=$date -Dboursorama.login=l -Dboursorama.password=p -Dabc.login=l -Dabc.password=p"

java -Dspring.profiles.active=dev -DJOB_NAME=jobMajAbcLibelles $vmOptions -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 1
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobMajAbcCotations $vmOptions -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 2
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobMajBoursoramaCours $vmOptions -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 3
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobConvertirEnValeurCours $vmOptions -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 4
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobCalculerMoyennes $vmOptions -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 5
fi


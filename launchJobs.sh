#!/bin/bash

date=$(date +"%d/%m/%Y")
dir=target

java -Dspring.profiles.active=dev -DJOB_NAME=jobMajAbcLibelles -DDATE=$date -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 1
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobMajAbcCotations -DDATE=$date -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 2
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobAbcToValeurCours -DDATE=$date -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 3
fi

java -Dspring.profiles.active=dev -DJOB_NAME=jobCalculerMoyennes -DDATE=$date -jar $dir/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 4
fi

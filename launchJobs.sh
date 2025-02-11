#!/bin/bash

date=$(date +"%d/%m/%Y")

java -DJOB_NAME=jobMajAbcLibelles -DDATE=$date -jar target/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 1
fi

java -DJOB_NAME=jobMajAbcCotations -DDATE=$date -jar target/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 2
fi

java -DJOB_NAME=jobAbcToValeurCours -DDATE=$date -jar target/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 3
fi

java -DJOB_NAME=jobCalculerMoyennes -DDATE=$date -jar target/aspirateur-cotations-boursieres-0.0.1-SNAPSHOT.jar

if [ $? != 0 ]; then
  exit 4
fi

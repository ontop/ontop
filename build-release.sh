#!/usr/bin/env bash
./mvnw clean install -DskipTests -Prelease
cd protege
../mvnw clean install -DskipTests
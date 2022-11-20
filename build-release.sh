#!/usr/bin/env bash
./mvnw clean package -DskipTests -Prelease
cd protege
../mvnw clean package -DskipTests
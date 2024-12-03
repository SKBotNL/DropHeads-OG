#!/usr/bin/bash

# Fetch all submodule content.
git submodule update --force --recursive --init --remote

# Pre-build EvLib-OG.
cd libs/EvLib-OG

./gradlew clean build eclipse

cd ../..

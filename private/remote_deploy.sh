#!/usr/bin/env bash

# Go to base folder
cd ..

# Remote
remote="user@machine.com"
targetdir="/folder/to/hold"

# Items to copy
declare -a items=(
	"analysis"
	"private"
	"paths-cache"
	"NetBench.jar"
)

# Delete and create directory
ssh ${remote} "mkdir ${targetdir}/netbench"

# Copy specific items
for item in "${items[@]}"
do
	scp -r ${item} ${remote}:"${targetdir}/netbench"
done

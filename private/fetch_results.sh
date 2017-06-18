#!/usr/bin/env bash

echo "Copying results..."
rsync -ravHe ssh --ignore-existing user@machine.com:/path/to/netbench/temp/results ../temp
`
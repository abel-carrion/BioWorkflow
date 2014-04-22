#!/bin/bash

<COMMANDS>

for i in {0..<NCOMMANDS>}
do
        ${commands[$i]}
        status=$?
        if [ ${status} -ne 0 ]; then
                echo "Script <EXECUTIONID> exited with code ${status}"
                exit;
        fi
done
echo "Script <EXECUTIONID>.sh has exited with code 0"
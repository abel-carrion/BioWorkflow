#!/bin/bash

cd $HOME/<EXECUTIONID>

<COMMANDS>

for i in {0..<NCOMMANDS>}
do
        eval ${commands[$i]}
        status=$?
        if [ ${status} -ne 0 ]; then
                echo "Script <SCRIPTNAME> exited with code ${status}" 
                exit;
        fi
done
echo "Script <SCRIPTNAME>.sh has exited with code 0" 
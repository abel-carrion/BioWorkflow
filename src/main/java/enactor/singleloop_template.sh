#!/bin/bash

<LNS>
<DIR>
<COMMANDS>

JOBID=$PWD

for i in {0..<NCOMMANDS>}
do
        eval ${commands[$i]}
        status=$?
        if [ ${status} -ne 0 ]; then
                echo "Script <JOBID>.sh exited with code ${status}"
                exit;
        fi
done
echo "Script <JOBID>.sh has exited with code 0" 
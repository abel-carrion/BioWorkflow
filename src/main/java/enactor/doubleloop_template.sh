#!/bin/bash

<LNS>
<COMMANDS>

for i in `ls -d <DIR>`
do
        for j in {0..<NCOMMANDS>}
        do
                eval ${commands[$j]}
                status=$?
                if [ ${status} -ne 0 ]; then
                        echo "Script <JOBID> exited with code ${status}"
                        exit;
                fi
        done
done
echo "Script <JOBID>.sh has exited with code 0"
ln -s <DIR> .
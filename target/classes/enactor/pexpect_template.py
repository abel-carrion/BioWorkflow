#!/usr/bin/python

import glob
import pexpect
files = ''
for file in glob.glob("<DIR1>/<FILES1>"):
    files = files + " " + file
child = pexpect.spawn('scp '+files+' <USER2>@<HOST2>:<DIR2>/<FILES2>')
child.timeout=9000
i = child.expect(['password:', r"yes/no"], timeout=9000)
if i == 0:
    child.sendline("<PASSWORD2>")
elif i == 1:
    child.sendline("yes")
    child.expect("password:", timeout=9000)
    child.sendline("<PASSWORD2>")
data = child.read()
child.close()
print 'Script <JOBID>.sh has exited with code 0'
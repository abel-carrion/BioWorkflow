import pexpect
child = pexpect.spawn('scp <USER1>@<HOST1>:<DIR1><FILE1> <DIR2><FILE2>')
child.expect('password:')
child.sendline('<PASSWORD1>')
data = child.read()
child.close()

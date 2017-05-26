# javaMasterSlave

This project is create by Yu Shu. Just and only for record purpose.

Compile:
Go to the source folder "masterslave".
Run "make" in terminal.

Example:
Run: (go up a folder first)
java masterslave.masterbot -p 6000
java masterslave.slavebot -p 6000 -h localhost

Tested with following command at master side:
// list all available slaves.
list
// let all slaves connect to www.sjsu.edu, each slave creats 1 connection.
connect all www.sjsu.edu 80
// let all slaves connect to www.sjsu.edu, each slave creats 2 connection.
connect all www.sjsu.edu 80 2
// let all slaves disconnect with www.sjsu.edu with port 80
disconnect all www.sjsu.edu 80
// let all slaves disconnect with www.sjsu.edu with all ports
disconnect all www.sjsu.edu
// add keepalive option
connect all www.sjsu.edu 80 keepalive
// creat random strings and let google search, drop all replies
connect all www.google.com 80 2 url=/#q=
// let all slaves test a range of IPs (by using ICMP echo)
ipscan all 4.2.2.2-4.2.2.8
// let specific slaves test a range of IPs (by using ICMP echo)
ipscan 127.0.0.1 4.2.2.2-4.2.2.8
// let specific slaves (chose by name) test a range of IPs (by using ICMP echo)
ipscan localhost 4.2.2.2-4.2.2.8
// let specific slaves (chose by name) test a range of TCP ports
tcpportscan localhost www.sjsu.edu 79-81
// let all slaves (chose by name) test a range of TCP ports
tcpportscan all www.sjsu.edu 79-81
// test geological information
geoipscan all 4.2.2.2-4.2.2.8
geoipscan localhost 4.2.2.2-4.2.2.8
geoipscan 127.0.0.1 208.80.153.203-208.80.153.205 


Version 4.0
geoipscan 127.0.0.1 208.80.153.203-208.80.153.205
// ip of wikipedia

Version 3.0
Add ip scan and tcp port scan feature for a range of IPs.

Version 2.0
Add keepalive, random postfixes for DDoS attack.

Version 1.0
connect slave target port repeats
disconnect slave target port

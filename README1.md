Compile:
Go to the source folder;
Run "make" in terminal.

Run: (go up a folder first)
java masterslave.masterbot -p 6000
java masterslave.slavebot -p 6000 -h localhost


Tested with following command:
list
connect all www.sjsu.edu 80
connect all www.sjsu.edu 80 2
disconnect all www.sjsu.edu 80
disconnect all www.sjsu.edu
connect all www.sjsu.edu 80 keepalive
connect all www.google.com 80 2 url=/#q=
ipscan all 4.2.2.2-4.2.2.8
ipscan 127.0.0.1 4.2.2.2-4.2.2.8
ipscan localhost 4.2.2.2-4.2.2.8
tcpportscan localhost www.sjsu.edu 79-81
tcpportscan all www.sjsu.edu 79-81
geoipscan all 4.2.2.2-4.2.2.8
geoipscan localhost 4.2.2.2-4.2.2.8
geoipscan 127.0.0.1 208.80.153.203-208.80.153.205

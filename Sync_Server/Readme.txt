Purpose : Simulate Lamports clocks in synchronization

Scenario : There are 3 server programs running. There is a single client program. 
The client selects arbitrarily one of the servers and issues file manipulation commands ( create
edit, delete). The selected server initially executes the commands in the remote servers and then 
executes the command if the remote execution on both the servers are successful. 

To run 
1) Copy the entire folder to your work folder.
2) update the serverlist file with IP address of the servers where your servers are running
3) Compile each of the servers and clients java files
4) run the server and then run the client. 
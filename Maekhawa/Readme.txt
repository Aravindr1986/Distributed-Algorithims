Purpose : Simulate Maekawa's algorithm

Scenario : There are 7 clients and 3 servers. Each client will select a server arbitrarily and tries to write a predefined text to it(<client no,no of write,Ip address : client no,no of write,Ip address>). Once a server accepts a client request, it then does not allow any other request. Implement Maekawa's algorithm for mutual exclusion for resolution of race conditions.

How to run
----------
1) Copy the file to the work folders.
2) Update all the server and client list( Note for each client folder update the ip address of qorom clients only). 
3) Run all the servers and then the clients.
4) Run the start program to trigger the whole sequence.

References :
https://en.wikipedia.org/wiki/Maekawa%27s_algorithm
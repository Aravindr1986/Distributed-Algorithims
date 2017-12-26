rm ../S1/file.txt
rm ../S2/file.txt
rm ../S3/file.txt
cp ../Client.java ../C1/ 
cp ../Client.java ../C2/
cp ../Client.java ../C3/
cp ../Client.java ../C4/
cp ../Client.java ../C5/
cp ../Client.java ../C6/
cp ../Client.java ../C7/
cp ../Server.java ../S1/
cp ../Server.java ../S2
cp ../Server.java ../S3/ 
javac Start_code.java
cd ../C1
javac Client.java
cd ../C2
javac Client.java
cd ../C3
javac Client.java
cd ../C4
javac Client.java
cd ../C5
javac Client.java
cd ../C6
javac Client.java
cd ../C7
javac Client.java
cd ../S1
javac Server.java
cd ../S2
javac Server.java
cd ../S3
javac Server.java

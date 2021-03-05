# SOE-YCSCS1/CS143: Compilers, Stanford University
This repository contains the assignment implementation of SOE-YCSCS1/CS143 Compilers, Stanford University
## Setup
Following is the version of ubuntu I used for all assignment, I directly setup the environment on this machine
```
Distributor ID:	Ubuntu
Description:	Ubuntu 16.04.7 LTS
Release:	16.04
Codename:	xenial
```
+ Install Packages (I have the jdk 11 installed)
```
sudo apt-get install flex bison build-essential csh libxaw7-dev
```
+ Make the /usr/class directory:
```
$ sudo mkdir /usr/class
```
+ Make the directory owned by you:
```
$ sudo chown $USER /usr/class
```
+ Go to /usr/class and download the tarball:
```
$ cd /usr/class
$ wget https://courses.edx.org/asset-v1:StanfordOnline+SOE.YCSCS1+1T2020+type@asset+block@student-dist.tar.gz -O student-dist.tar.gz
```
+ Untar:
```
$ tar -xf student-dist.tar.gz
```
+ Add a symlink to your home directory:
```
$ ln -s /usr/class/cs143/cool ~/cool
```
+ Add the bin directory to your $PATH environment variable. If you are using bash, add to your .profile (or .bash_profile, etc. depending on your configuration; note that in Ubuntu have to log out and back in for this to take effect):
```
PATH=/usr/class/cs143/cool/bin:$PATH
```

## PA2J
All project files are in ./assignments/PA2J, includes necessary lib to run following tests

./assignments/java_cup is needed when using pa1-grading.pl, otherwise will see java_cup.runtime.Scanner cannot find
+ Compile lextest program
```
make lexer
```
+ Then run lexer by putting any test .cl file, e.g
```
lexer hello_world.cl
lexer test.cl
```
+ Or complie and run lexer on the test.cl
```
make dotest
```
+ Remove all builds
```
make clean
```
+ Check lexical analyzer runs with parser, semantic analysis and code generation.
```
make
mycoolc hello_world.cl
spim hello_word.s
```
+ Use grading tool
```
perl pa1-grading.pl
```
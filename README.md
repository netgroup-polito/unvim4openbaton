# OpenBaton UniversalNode Driver

[OpenBaton](http://openbaton.github.io/index.html) is an open source project providing a reference implementation of the NFVO and VNFM based on the ETSI specification, is implemented in java using the spring.io framework. It consists of two main components: a NFVO and a generic VNFM.
This project **unvim4openbaton** contains an implementation of a plugin for OpenBaton system.

## How to compile the UniversalNode Driver
Execute the following commands:
```sh
$ cd [unvim4openbaton]
$ ./gradlew build
```
Ater the compilation you can find the .jar file in:
```sh
[unvim4openbaton]/build/libs
```

## How to use the UniversalNode Driver
There are two way to run the UniversalNode Driver:

 - Let Openbaton know about the presence of the UniversalNode Driver:

	```sh
	$ cp [unvim4openbaton]/buil/libs/unvim4openbaton.jar /opt/openbaton/nfvo/plugins/vim-drivers
	```
	Finally restart Openbaton
 - Running directly the generated .jar file:

	```sh
	$ java -jar  [unvim4openbaton]/buil/libs/unvim4openbaton.jar unvim [rabbitmq-ip] [rabbitmq-port] [n-of-consumers] [user] [password]

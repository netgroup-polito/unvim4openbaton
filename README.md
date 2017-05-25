# Open Baton Universal Node Driver

[Open Baton](http://openbaton.github.io/index.html) is an open source project providing a reference implementation of the NFVO and VNFM based on the ETSI NFV MANO specification. It is implemented in Java using the spring.io framework and consists of two main components: a NFVO and a generic VNFM.
The NFVO communicate with all the underlying VIMs through an interface designed into the MANO specification (NFVO-VIM reference point). However the VIM-like tools provided by the current state of the art (e.g., OpenStack, Universal Node) does not implement a MANO compliant interface, hence in order to overcome such a problem Open Baton places a VIM plugin between the NFVO and the actual VIM. The VIM plugin receives requests through the NFVO-VIM reference point and satisfies them exploiting the network primitives provided by the underlying VIM.

This project **unvim4openbaton** contains a VIM plugin implementation for the Universal Node support into the Open Baton system.

Moreover, it contains additional documentation about the Open Baton orchestration architecture and how to make a complete installation of the system by scratch.

## How to compile the Universal Node Driver
Execute the following commands:
```sh
$ cd [unvim4openbaton]
$ ./gradlew build
```
Ater the compilation you can find the .jar file in:
```sh
[unvim4openbaton]/build/libs
```

## How to use the Universal Node Driver
This guide supposes that all the components required by the Open Baton (except for the VIM plugin) and by the Universal Node architectures are already running. See [README_RUN](README_RUN.md) for more information.

There are two way to run the UniversalNode Driver:

 - Inform Open Baton about the Universal Node Driver:

	```sh
	$ cp [unvim4openbaton]/buil/libs/unvim4openbaton.jar /opt/openbaton/nfvo/plugins/vim-drivers
	```
	Finally restart Open Baton
 - Running directly the generated .jar file:

	```sh
	$ java -jar  [unvim4openbaton]/buil/libs/unvim4openbaton.jar unvim [rabbitmq-ip] [rabbitmq-port] [n-of-consumers] [user] [password]

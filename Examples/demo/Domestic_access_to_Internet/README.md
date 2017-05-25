# Domestic access to the Internet
This demo aims to show how the performance (in terms of bandwidth) improves as the resources move closer to the end user.
For this purpose, we measured the throughput between a client and the storage server with the *iperf* tool in three different scenarios:
* All the VNFs deployed in the Universal Node
* All the VNFs deployed in OpenStack
* All the VNFs deployed in OpenStack but the client and the storage server

![drawing](Pictures/domestic_access_to_the_internet.jpg)

## Deployment information
* All the VNF described into the NSD refer to the same [template](https://github.com/netgroup-polito/unvim4openbaton/tree/version_3.2.1/Examples/demo/Domestic_access_to_the_Internet/ubuntu.json)
* The image that the ubuntu template refers to (and that is used in order to deploy a VNF) can be found [here](https://cloud-images.ubuntu.com/)
* The UN configurable VNFs (and templates) can be found [here](https://github.com/netgroup-polito/frog4-configurable-vnf)

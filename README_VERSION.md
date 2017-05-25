# Plugin Version
This plugin supports the Open Baton version 3.2.1.

Difference from the previous plugin version (3.1.0):
* the VIM plugin interface provided by Open Baton has been updated, reference to networks are turned into 'VNFDConnectionPoint' from 'String'  
*  unvim4openbaton/src/main/java/org/polito/unvim/UnClient.java:launchInstanceAndWait - network parameter type updated
*  unvim4openbaton/src/main/java/org/polito/unvim/UnClient.java:launchInstance - network parameter type updated
*  unvim4openbaton/src/main/java/org/polito/management/ComputeManager.java:createServer - network parameter type updated, network are now uniquely identified by 'name' 
   


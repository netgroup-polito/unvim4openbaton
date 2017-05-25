# Run the orchestration environment
In order to run all the orchestartion environment:
* run Open Baton orchestrator and generic VNFM
* run the Universal Node orchestrator with the ancillary services
* run the UN VIM plugin

The execution order of the first two is not importan , but it is mandatory that the VIM plugin is executed last.

## Run the Open Baton system
Start the Open Baton orchestrator
```sh
$ cd /opt/openbaton/nfvo
$ ./openbaton.sh start
```

Start the VNFM
```sh
$ cd /opt/openbaton/generic-vnfm
$ ./generic-vnfm.sh start
```

The RabbitMQ message bus should be already running

## Run the Universal Node system
Start the storage server
```sh
$ cd [FROG4-datastore]
$ ./start.sh
```

Start the message bus
```sh
$ ddbroker -r tcp://[IP address]:[port] -k [broker keys] -s 1/2/3
```

Start the configuration service
```sh
$ cd [FROG4-configuration service]
$ source .env/bin/activate
$ sudo python3 manage.py runserver
```

Start the Universal Node orchestator
```sh
$ cd [un-orchestator]/orchestrator
$ ./node-orchestrator --d config/[config.ini]
```

## Run the UN VIM plugin
See the main README
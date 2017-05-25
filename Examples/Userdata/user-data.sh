#!/bin/bash

export MONITORING_IP=
export TIMEZONE=
export BROKER_IP=
export BROKER_PORT=
export USERNAME=
export PASSWORD=
export EXCHANGE_NAME=
export EMS_HEARTBEAT=
export EMS_AUTODELETE=
export EMS_VERSION=
export ENDPOINT=

# Retries a command on failure.
# $1 - the max number of attempts
# $2... - the command to run
retry() {
    local -r -i max_attempts="$1"; shift
    local -r cmd="$@"
    local -i attempt_num=1
 
    until $cmd
    do
        if (( attempt_num == max_attempts ))
        then
            echo "Attempt $attempt_num failed and there are no more attempts left!"
            return 1
        else
            echo "Attempt $attempt_num failed! Trying again in 3 + $attempt_num seconds..."
            sleep $(( attempt_num++ ))
        fi
    done
}


sudo ifconfig eth0 up
sudo ifconfig eth0 mtu 1400
sudo dhclient -v eth0
# for all the interfaces the mtu must be setted!
sudo ifconfig eth1 up
sudo ifconfig eth1 mtu 1400
sudo dhclient -v eth1

result=$(dpkg -l | grep "ems-$EMS_VERSION" | wc -l)
if [ ${result} -eq 0 ]; then
	echo "Downloading EMS from internet"
	echo "deb http://get.openbaton.org/repos/apt/debian/ ems main" >> /etc/apt/sources.list
retry 5	wget -O - http://get.openbaton.org/public.gpg.key | apt-key add -
retry 5	apt-get update
	cp /usr/share/zoneinfo/$TIMEZONE /etc/localtime
retry 5 	apt-get install git -y
retry 5 	apt-get install -y ems-$EMS_VERSION
else
	echo "EMS is already installed"
fi
if [ -z "$MONITORING_IP" ]; then
	echo "No MONITORING_IP is defined, i will not download zabbix-agent"
else
	echo "Installing zabbix-agent for server at $MONITORING_IP"
retry 5	sudo apt-get install -y zabbix-agent
retry 5	sudo sed -i -e "s/ServerActive=127.0.0.1/ServerActive=$MONITORING_IP:10051/g" -e "s/Server=127.0.0.1/Server=$MONITORING_IP/g" -e "s/Hostname=Zabbix server/#Hostname=/g" /etc/zabbix/zabbix_agentd.conf
retry 5	sudo service zabbix-agent restart
retry 5	sudo rm zabbix-release_2.2-1+precise_all.deb
	echo "finished installing zabbix-agent!"
fi
mkdir -p /etc/openbaton/ems
echo [ems] > /etc/openbaton/ems/conf.ini
echo broker_ip=$BROKER_IP >> /etc/openbaton/ems/conf.ini
echo broker_port=$BROKER_PORT >> /etc/openbaton/ems/conf.ini
echo username=$USERNAME >> /etc/openbaton/ems/conf.ini
echo password=$PASSWORD >> /etc/openbaton/ems/conf.ini
echo exchange=$EXCHANGE_NAME >> /etc/openbaton/ems/conf.ini
echo heartbeat=$EMS_HEARTBEAT >> /etc/openbaton/ems/conf.ini
echo autodelete=$EMS_AUTODELETE >> /etc/openbaton/ems/conf.ini
export hn=`hostname`
echo type=$ENDPOINT >> /etc/openbaton/ems/conf.ini
echo hostname=$hn >> /etc/openbaton/ems/conf.ini
sleep 20
service ems restart

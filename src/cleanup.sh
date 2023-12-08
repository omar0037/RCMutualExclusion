#!/bin/bash

# Change this to your netid
netid=oxs230000

# Root directory of your project
PROJDIR=$HOME/advOs

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

n=0
cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e "s/\r$//" |
(
    read i
    numOfNodes=$( echo $i | awk '{ print $1 }' )
    input+=$numOfNodes
 while [[ $n -lt $numOfNodes ]]
 do
 	read line
	host=$(echo $line | awk '{print $2}')
    ssh ${netid}@${host}.utdallas.edu "killall -u $netid" &
    #gnome-terminal "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host.utdallas.edu killall -u $netid" &
	n=$((n+1))
 done
)

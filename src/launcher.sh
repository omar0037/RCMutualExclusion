#!/bin/bash

# Change this to your netid
netid=oxs230000

# Root directory of your project
PROJDIR=$HOME/advOs

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/launch/config.txt

touch common.txt
#touch responseTime.txt
#touch messageComplexity.txt
#touch sysThroughput.txt

#echo "0" > responseTime.txt
#echo "0" > messageComplexity.txt
#echo "0" > sysThroughput.txt

n=0

declare -A matrix


input=""

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e "s/\r$//" |
(
    read i
    numOfNodes=$( echo $i | awk '{ print $1 }' )
    input+=$numOfNodes
    input+=' '
    minPerAct=$(echo $i | awk '{print $2}')
    input+=$minPerAct
    input+=' '
    maxPerAct=$(echo $i | awk '{print $3}')
    input+=$maxPerAct
    input+=' '
    minSendDel=$(echo $i | awk '{print $4}')
    input+=$minSendDel
    input+=' '
    input+=" -"
    #echo $numOfNodes
 while [[ $n -lt $numOfNodes ]]
 do
 	read line
 	#echo $line
	node=$(echo $line | awk '{print $1}')
	host=$(echo $line | awk '{print $2}')
	port=$(echo $line | awk '{print $3}')
	matrix["$node"]=$node
	matrix["$node"]+=' '
	matrix["$node"]+=$host
	matrix["$node"]+=' '
	matrix["$node"]+=$port
	#matrix["$node"]+=' '
	n=$((n+1))
 done
 n=0
  while [[ $n -lt $numOfNodes ]]
  do
         #read line
         #matrix["$n"]+=$line
         input+=${matrix[$n]}
         input+=" -"
         n=$((n+1))
  done

 n=0
 netid=oxs230000
  while [[ $n -lt $numOfNodes ]]
   do
     host=$(echo ${matrix["$n"]} | awk '{print $2}')
	   #a=${matrix[$n]}
	   #echo "------------------------------------------"
	   #echo $input $a
	   #ssh ${netid}@${host}.utdallas.edu "cd advOs && java Node $input" &
    	   gnome-terminal -e  "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host; cd advOs2; java Node $input; exec bash" &
	   n=$((n+1))
   done
 )

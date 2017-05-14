#!/bin/bash

cd `dirname $0`

# disable power led
echo 1 | sudo tee /sys/class/leds/led0/brightness > /dev/null

cd python3

sudo python3 beacon_scanner.py

# enable power led
echo 0 | sudo tee /sys/class/leds/led0/brightness > /dev/null

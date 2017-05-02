# test BLE Scanning software

import blescan
import signal
import sys
import time
import datetime
import socket

import urllib2

import bluetooth._bluetooth as bluez

dev_id = 0
uuid = "livingroom"
server_url = "http://precision:8080/tracker/"
interval = 2.0

def signal_handler(signal, frame):
	print 'ble thread stopped'
	blescan.finalizeScan(sock,oldFilter)
	sys.exit(0)

signal.signal(signal.SIGTERM, signal_handler)
signal.signal(signal.SIGINT, signal_handler)

try:
	sock = bluez.hci_open_dev(dev_id)
	print "ble thread started"
except:
	print "error accessing bluetooth device..."
	sys.exit(1)

blescan.hci_le_set_scan_parameters(sock)
blescan.hci_enable_le_scan(sock)

hasError = None

oldFilter = blescan.prepareScan(sock)
start = time.time()

while True:
	returnedList = blescan.scanBeacons(sock,interval)
	
	end = time.time()
	interval = ( end - start )
	start = time.time()

	json = "{"
	json += "\"uuid\":\"" + uuid + "\","
	json += "\"interval\":"+interval+","
    json += "\"trackedBeacons\":["

	devices = []

	for key in returnedList:
		
		beacon = returnedList[key]
		
		device = "{"
		device += "\"uuid\":\""+beacon["uuid"]+"\","
		device += "\"txpower\":"+str(beacon["txpower"])+","
		device += "\"rssi\":"+str(beacon["rssi"])+","
		device += "\"samples\":"+str(beacon["samples"])+","
		device += "}"
		devices.append(device)
		#adstring += "MAC: " + beacon["mac"]
		#adstring += ", MAJOR: " + beacon["major"]
		#adstring += ", MINOR: " + beacon["minor"]
		
	json += ",".join(devices)
	
	#ts = time.time()
	#st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
	#print st + " " + json

	json += "]}"

	req = urllib2.Request(server_url, json)
	try:
		response = urllib2.urlopen(req)
		
		if hasError:
			print 'ble thread resumed'
			hasError = None
	except urllib2.HTTPError, e:
		print 'http error: ' + str( e.code )
		hasError = True
	except urllib2.URLError, e:
		print 'url error: ' + str(e.args)
		hasError = True
	except socket.error, e:
		print 'socket error: ' + str(e.args)
		hasError = True

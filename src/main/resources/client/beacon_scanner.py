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

lastJson = None

oldFilter = blescan.prepareScan(sock)

while True:
    start = time.time()
    
    returnedList = blescan.scanBeacons(sock,interval)
    
    json = "{"
    json += "\"uuid\":\"" + uuid + "\","
    json += "\"trackedBeacons\":["

    devices = []
    
    maxSamples = 0
    for key in returnedList:
        
        beacon = returnedList[key]
        
        device = "{"
        device += "\"mac\":\""+beacon["mac"]+"\","
        device += "\"uuid\":\""+beacon["uuid"]+"\","
        device += "\"major\":\""+beacon["major"]+"\","
        device += "\"minor\":\""+beacon["minor"]+"\","
        device += "\"samples\":["

        samples = []
        for beaconSample in beacon["samples"]:
            
            sample = "{"
            sample += "\"txpower\":" + str(beaconSample['txpower']) + ","
            sample += "\"rssi\":" + str(beaconSample['rssi']) + ","
            sample += "\"timestamp\":" + str(beaconSample['timestamp'])
            sample += "}"
            samples.append(sample)
            
            if maxSamples < len(samples):
                maxSamples = len(samples)
            
        device += ",".join(samples)
        device += "]}"

        devices.append(device)
        
    json += ",".join(devices)
    
    json += "]}"

    req = urllib2.Request(server_url, json)
    try:
        if json != lastJson:
            if maxSamples > 25:
                end = time.time()
                st = datetime.datetime.fromtimestamp(end).strftime('%Y-%m-%d %H:%M:%S')
                print st + " - CNT: " + str(maxSamples) + " - TIME: " + str(end - start) + " - JSON: " + json + "\n"

            response = urllib2.urlopen(req)
            lastJson = json
        else:
            print 'json not changed'
    except urllib2.HTTPError, e:
        print 'http error: ' + str( e.code )
        hasError = True
    except urllib2.URLError, e:
        print 'url error: ' + str(e.args)
        hasError = True
    except socket.error, e:
        print 'socket error: ' + str(e.args)
        hasError = True

# test BLE Scanning software

import datetime
import signal
import socket
import sys
import time

import blescan
import bluetooth._bluetooth as bluez
import commands
import urllib2

dev_id = 0
interval = 2.0
server_url = "http://precision:8080/tracker/"
ip_map = {
    '192.168.0.125': 'livingroom',
    '192.168.0.126': 'kitchen',
    '192.168.0.127': 'floor',
}


def getUUID():
    ip = commands.getoutput('hostname -I').strip()
    try:
        return ip_map[ip]
    except KeyError, e:
        return None


def log(line):
    st = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
    print
    st + " - " + line


def signal_handler(signal, frame):
    log('ble thread stopped')
    blescan.finalizeScan(sock, oldFilter)
    sys.exit(0)


signal.signal(signal.SIGTERM, signal_handler)
signal.signal(signal.SIGINT, signal_handler)

uuid = getUUID()

if uuid == None:
    log("no uuid detected")
    exit(1)
else:
    log(uuid + " detected")

try:
    sock = bluez.hci_open_dev(dev_id)
    log("ble thread started")
except:
    log("error accessing bluetooth device...")
    sys.exit(1)

log("set le parameter")
blescan.hci_le_set_scan_parameters(sock)

log("enable le scanning")
blescan.hci_enable_le_scan(sock)

lastJson = None

oldFilter = blescan.prepareScan(sock)

while True:
    start = time.time()

    returnedList = blescan.scanBeacons(sock, interval)

    json = "{"
    json += "\"uuid\":\"" + uuid + "\","
    json += "\"trackedBeacons\":["

    devices = []

    maxSamples = 0
    for key in returnedList:

        beacon = returnedList[key]

        device = "{"
        device += "\"mac\":\"" + beacon["mac"] + "\","
        device += "\"uuid\":\"" + beacon["uuid"] + "\","
        device += "\"major\":\"" + beacon["major"] + "\","
        device += "\"minor\":\"" + beacon["minor"] + "\","
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
                log("CNT: " + str(maxSamples) + " - TIME: " + str(time.time() - start) + " - JSON: " + json + "\n")

            response = urllib2.urlopen(req)
            lastJson = json
        else:
            log('json not changed')
    except urllib2.HTTPError, e:
        log('http error: ' + str(e.code))
        hasError = True
    except urllib2.URLError, e:
        log('url error: ' + str(e.args))
        hasError = True
    except socket.error, e:
        log('socket error: ' + str(e.args))
        hasError = True

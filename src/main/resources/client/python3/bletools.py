import subprocess
import datetime
import time

def log(line):
	st = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
	print( st + " - " + line )

def getUUID(ip_map):
	ip = subprocess.check_output(["hostname", "-I"], universal_newlines=True).strip()
	try:
		return ip_map[ip]
	except KeyError as e:
		return None

def convertTolJson( myFullList, uuid ):
	json = "{"
	json += "\"uuid\":\"" + uuid + "\","
	json += "\"trackedBeacons\":["

	devices = []

	maxSamples = 0
	for key in myFullList:

		beacon = myFullList[key]

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

	return (json,maxSamples)

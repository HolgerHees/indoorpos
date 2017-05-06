import subprocess
import datetime
import time


def log(line):
    st = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
    print(st + " - " + line)


def get_uuid(ip_map):
    ip = subprocess.check_output(["hostname", "-I"], universal_newlines=True).strip()
    try:
        return ip_map[ip]
    except KeyError:
        return None


def convert_to_json(my_full_list, uuid, max_samples):
    json = "{"
    json += "\"uuid\":\"" + uuid + "\","
    json += "\"trackedBeacons\":["

    devices = []

    sample_count = 0
    for key in my_full_list:

        beacon = my_full_list[key]

        device = "{"
        device += "\"mac\":\""+beacon["mac"]+"\","
        device += "\"uuid\":\""+beacon["uuid"]+"\","
        device += "\"major\":\""+beacon["major"]+"\","
        device += "\"minor\":\""+beacon["minor"]+"\","
        device += "\"samples\":["

        if len(beacon["samples"]) > max_samples:
            beacon["samples"] = beacon["samples"][((max_samples + 1) * -1):-1]

        samples = []
        for beaconSample in beacon["samples"]:

            sample = "{"
            sample += "\"txpower\":" + str(beaconSample['txpower']) + ","
            sample += "\"rssi\":" + str(beaconSample['rssi']) + ","
            sample += "\"timestamp\":" + str(beaconSample['timestamp'])
            sample += "}"
            samples.append(sample)

            if sample_count < len(samples):
                sample_count = len(samples)

        device += ",".join(samples)
        device += "]}"

        devices.append(device)

    json += ",".join(devices)

    json += "]}"

    return json, sample_count

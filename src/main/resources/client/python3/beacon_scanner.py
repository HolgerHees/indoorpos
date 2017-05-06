import signal
import sys
import time
import socket
import websockets
import bluetooth._bluetooth as bluez
import asyncio
import functools
import math
from concurrent.futures import CancelledError
import bleproto
import bletools

dev_id = 0
interval = 2.0
frequency = 0.1
server_url = "ws://precision:8080/trackerUpdate"
ip_map = {
    '192.168.0.125': 'livingroom',
    '192.168.0.126': 'kitchen',
    '192.168.0.127': 'floor',
}


def loop_signal_handler(loop):
    for task in asyncio.Task.all_tasks():
        task.cancel()
    bletools.log('ble scan stopped')
    bleproto.finalize_scan(sock, oldFilter)
    sys.exit(0)

loop = asyncio.get_event_loop()
loop.add_signal_handler(signal.SIGTERM, functools.partial(loop_signal_handler, loop))
loop.add_signal_handler(signal.SIGINT, functools.partial(loop_signal_handler, loop))
loop.add_signal_handler(signal.SIGHUP, functools.partial(loop_signal_handler, loop))

uuid = bletools.get_uuid(ip_map)

if uuid is None:
    bletools.log("no uuid detected")
    exit(1)

try:
    bletools.log("open device hci%s" % str(dev_id))
    sock = bluez.hci_open_dev(dev_id)
except bluez.error as e:
    bletools.log("error accessing bluetooth device...")
    sys.exit(1)

bleproto.hci_le_set_scan_parameters(sock)
bleproto.hci_enable_le_scan(sock)
oldFilter = bleproto.prepare_scan(sock)

@asyncio.coroutine
def scan_beacons(interval_start):
    
    my_full_list = {}
    interval_end = 0
    while True:
        try:
            my_full_list = bleproto.scan_beacons(sock, my_full_list)
            # check for max runtime
            interval_end = time.time()
            runtime = interval_end - interval_start
            if runtime >= interval:
                break
        except bluez.error:
            # check for max runtime
            interval_end = time.time()
            runtime = interval_end - interval_start
            if runtime >= interval:
                break
            # wait for new data
            interval_left = interval - runtime
            if interval_left > frequency:
                yield from asyncio.sleep(frequency)
            else:
                yield from asyncio.sleep(interval_left)

    #bleproto.clear_discovered_devices(sock)

    interval_duration = str(interval_end - interval_start)
    interval_start = interval_end

    return my_full_list, interval_start, interval_end, interval_duration


@asyncio.coroutine
def main_loop():

    bleproto.clear_discovered_devices(sock)
                
    max_samples = int( math.ceil(interval / frequency) )

    last_json = None
    websocket = None

    try:
        while True:
            try:
                bletools.log("open websocket %s" % server_url)
                websocket = yield from websockets.connect(server_url)

                bletools.log("start ble scanning in the %s" % uuid)
                interval_start = time.time()
                while True:

                    my_full_list, interval_start, interval_end, interval_duration = yield from scan_beacons(interval_start)

                    json, sample_count = bletools.convert_to_json( my_full_list, uuid, max_samples )

                    #bletools.log("CNT: " + str(sample_count) + " - TIME: " + interval_duration + "\n")

                    if json != last_json:
                        yield from websocket.send(json)
                        last_json = json
            except (websockets.exceptions.ConnectionClosed, websockets.exceptions.InvalidHandshake, OSError, socket.error) as e:
                bletools.log("socket error: " + str(e.args))
                yield from asyncio.sleep(10.0)
    except CancelledError:
        pass
    finally:
        if websocket is not None:
            bletools.log("close websocket")
            yield from websocket.close()

asyncio.get_event_loop().run_until_complete(main_loop())

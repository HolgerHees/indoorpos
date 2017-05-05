import signal
import sys
import time
import socket
import websockets
import bluetooth._bluetooth as bluez
import asyncio
import functools
from concurrent.futures import CancelledError
import bleproto
import bletools

dev_id = 0
interval = 2.0
server_url = "ws://precision:8080/trackerUpdate"
ip_map = {
    '192.168.0.125': 'livingroom',
    '192.168.0.126': 'kitchen',
    '192.168.0.127': 'floor',
}


def loop_signal_handler(loop):
    for task in asyncio.Task.all_tasks():
        task.cancel()
    bletools.log('ble thread stopped')
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
else:
    bletools.log(uuid + " detected")

try:
    sock = bluez.hci_open_dev(dev_id)
    bletools.log("ble thread started")
except bluez.BluetoothError as e:
    bletools.log("error accessing bluetooth device...")
    sys.exit(1)

bletools.log("set le parameter")
bleproto.hci_le_set_scan_parameters(sock)

bletools.log("enable le scanning")
bleproto.hci_enable_le_scan(sock)

oldFilter = bleproto.prepare_scan(sock)


def scan_beacons(interval_start):
    my_full_list = {}
    interval_end = 0
    while True:
        try:
            my_full_list = bleproto.scan_beacons(sock, my_full_list)
        except bluez.error:
            pass
        finally:
            interval_end = time.time()
            runtime = interval_end - interval_start
            if runtime >= interval:
                break
            interval_left = interval - runtime
            if interval_left > 0.1:
                yield from asyncio.sleep(0.1)
            else:
                yield from asyncio.sleep(interval_left)

    bleproto.clear_discovered_devices(sock)

    interval_duration = str(interval_end - interval_start)
    interval_start = interval_end

    return my_full_list, interval_start, interval_end, interval_duration


@asyncio.coroutine
def main_loop():
    last_json = None
    websocket = None

    try:
        while True:
            try:
                websocket = yield from websockets.connect(server_url)
                bletools.log("socket opened")

                bleproto.clear_discovered_devices(sock)
                interval_start = time.time()

                while True:

                    my_full_list, interval_start, interval_end, interval_duration = yield from scan_beacons(interval_start)

                    json, max_samples = bletools.convert_to_json( my_full_list, uuid )

                    bletools.log("CNT: " + str(max_samples) + " - TIME: " + interval_duration + "\n")

                    if json != last_json:
                        if max_samples > 25:
                            bletools.log("CNT: " + str(max_samples) + " - TIME: " + interval_duration + " - JSON: " + json + "\n")

                        yield from websocket.send(json)
                        last_json = json
            except (OSError, socket.error) as e:
                bletools.log("socket error: " + str(e.args))
                yield from asyncio.sleep(10.0)
    except CancelledError:
        pass
    finally:
        if websocket is not None:
            yield from websocket.close()
            bletools.log("socket closed")

asyncio.get_event_loop().run_until_complete(main_loop())

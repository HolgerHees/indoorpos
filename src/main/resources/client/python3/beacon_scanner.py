import asyncio
import signal
import socket
import sys
import time

import bleproto
import bletools
import bluetooth._bluetooth as bluez
import functools
import websockets
from concurrent.futures import CancelledError

dev_id = 0
server_url = "ws://precision:8080/trackerUpdate"


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

ip = bletools.get_ip()

try:
    bletools.log("open device hci%s" % str(dev_id))
    sock = bluez.hci_open_dev(dev_id)
except bluez.error as e:
    bletools.log("error accessing bluetooth device...")
    sys.exit(1)

bleproto.hci_disable_le_advertise(sock)
bleproto.hci_le_set_scan_parameters(sock)
bleproto.hci_enable_le_scan(sock)
oldFilter = bleproto.prepare_scan(sock)


@asyncio.coroutine
def scan_beacons(interval_start, interval_end, beacon_frequency):
    my_full_list = {}
    current_time = 0
    while True:
        try:
            my_full_list = bleproto.scan_beacons(sock, my_full_list)
            current_time = time.time()
            if current_time >= interval_end:
                break
        except bluez.error:
            current_time = time.time()
            if current_time >= interval_end:
                break
            # wait for new data
            interval_left = interval_end - current_time
            if interval_left > beacon_frequency:
                yield from asyncio.sleep(beacon_frequency)
            else:
                yield from asyncio.sleep(interval_left)

    # bleproto.clear_discovered_devices(sock)

    interval_duration = current_time - interval_start
    interval_start = current_time

    return my_full_list, interval_start, interval_duration


@asyncio.coroutine
def main_loop():
    bleproto.clear_discovered_devices(sock)

    last_json = None
    websocket = None

    try:
        while True:
            try:
                bletools.log("open websocket %s" % server_url)
                websocket = yield from websockets.connect(server_url)

                interval_start = time.time()

                yield from websocket.send("init:%s" % ip);
                data = yield from websocket.recv();

                next_wakeup, interval_length, beacon_frequency, ping_interval, uuid = data.split(",")
                next_wakeup = int(next_wakeup) / 1000.0                
                interval_length = int(interval_length) / 1000.0
                beacon_frequency = int(beacon_frequency) / 1000.0
                ping_interval = int(ping_interval) / 1000.0
                
                max_samples = int(round(interval_length / beacon_frequency))

                bletools.log("start ble scanning in the %s" % uuid)
                interval_end = interval_start + interval_length + next_wakeup
                skip_count = 0

                while True:

                    my_full_list, interval_start, interval_duration = yield from scan_beacons(interval_start, interval_end, beacon_frequency)

                    json, sample_count = bletools.convert_to_json(my_full_list, uuid, max_samples)

                    # if json changed or every minute
                    if json != last_json or skip_count >= (ping_interval / interval_length):

                        # bletools.log("%i %i" % (skip_count, (ping_interval / interval_length)))

                        last_json = json
                        skip_count = 0

                        network_start = time.time()
                        yield from websocket.send(json)

                        next_wakeup = yield from websocket.recv();
                        network_end = time.time()

                        bletools.log(
                            "CNT: " + str(sample_count) + " - TIME: " + ("%.4f" % interval_duration) + " - NET: " + (
                            "%.4f" % (network_end - network_start)))

                        next_wakeup = int(next_wakeup) / 1000.0

                        #bletools.log(str(next_wakeup))

                        interval_end = interval_start + interval_length + next_wakeup
                    else:
                        skip_count += 1
                        interval_end = interval_start + interval_length

            except (
                    websockets.exceptions.ConnectionClosed, websockets.exceptions.InvalidHandshake, OSError,
                    socket.error) as e:
                bletools.log("socket error: " + str(e.args))
                yield from asyncio.sleep(10.0)
    except CancelledError:
        pass
    finally:
        if websocket is not None:
            bletools.log("close websocket")
            yield from websocket.close()


asyncio.get_event_loop().run_until_complete(main_loop())

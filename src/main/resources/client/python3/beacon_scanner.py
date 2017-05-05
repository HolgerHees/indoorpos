# test BLE Scanning software

import asyncio
import functools
import signal
import bluetooth._bluetooth as bluez
import socket
import sys
import time
import websockets
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
    bletools.log( 'ble thread stopped' )
    bleproto.finalizeScan(sock, oldFilter)
    sys.exit(0)

loop = asyncio.get_event_loop()
loop.add_signal_handler(signal.SIGTERM, functools.partial(loop_signal_handler, loop))
loop.add_signal_handler(signal.SIGINT, functools.partial(loop_signal_handler, loop))
loop.add_signal_handler(signal.SIGHUP, functools.partial(loop_signal_handler, loop))

uuid = bletools.getUUID(ip_map)

if uuid == None:
	bletools.log( "no uuid detected" )
	exit(1)
else:
	bletools.log( uuid + " detected" )

try:
    sock = bluez.hci_open_dev(dev_id)
    bletools.log( "ble thread started" )
except:
    bletools.log( "error accessing bluetooth device..." )
    sys.exit(1)

bletools.log( "set le parameter" )
bleproto.hci_le_set_scan_parameters(sock)

bletools.log( "enable le scanning" )
bleproto.hci_enable_le_scan(sock)

oldFilter = bleproto.prepareScan(sock)

@asyncio.coroutine
def mainLoop():
    lastJson = None
    websocket = None

    try:
        while True:
            try:
                websocket = yield from websockets.connect(server_url)
                bletools.log( "websocket opened" )

                bleproto.clearDiscoveredDevices(sock)
                interval_start = time.time()

                while True:
                    myFullList = {}
                    interval_end = 0
                    while True:
                        try:
                            myFullList = bleproto.scanBeacons(sock, myFullList)
                        except bluez.error as e:
                            pass
                        finally:
                            interval_end = time.time()
                            runtime = interval_end - interval_start
                            if runtime >= interval:
                                break
                            interval_left = interval - runtime
                            if interval_left > 0.1:
                                yield from asyncio.sleep( 0.1 )
                            else:
                                yield from asyncio.sleep( interval_left )

                    bleproto.clearDiscoveredDevices(sock)

                    interval_duration = str(interval_end - interval_start)
                    interval_start = interval_end

                    json, maxSamples = bletools.convertToJson( myFullList, uuid )

                    #bletools.log( "CNT: " + str(maxSamples) + " - TIME: " + interval_duration + " - JSON: " + json + "\n" )

                    if json != lastJson:
                        if maxSamples > 25:
                            bletools.log( "CNT: " + str(maxSamples) + " - TIME: " + interval_duration + " - JSON: " + json + "\n" )

                        yield from websocket.send(json)
                        lastJson = json
                    else:
                        #bletools.log( 'json not changed' )
                        pass
            except (OSError, socket.error) as e:
                bletools.log( "socke error: " + str(e.args) )
                yield from asyncio.sleep( 10.0 )
    except CancelledError as e:
        pass
    finally:
        if websocket != None:
            yield from websocket.close()
            bletools.log( "websocket closed" )

asyncio.get_event_loop().run_until_complete(mainLoop())

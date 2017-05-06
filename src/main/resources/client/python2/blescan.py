# BLE iBeaconScanner based on https://github.com/adamf/BLE/blob/master/ble-scanner.py
# JCS 06/07/14

DEBUG = False
# BLE scanner based on https://github.com/adamf/BLE/blob/master/ble-scanner.py
# BLE scanner, based on https://code.google.com/p/pybluez/source/browse/trunk/examples/advanced/inquiry-with-rssi.py

# https://github.com/pauloborges/bluez/blob/master/tools/hcitool.c for lescan
# https://kernel.googlesource.com/pub/scm/bluetooth/bluez/+/5.6/lib/hci.h for opcodes
# https://github.com/pauloborges/bluez/blob/master/lib/hci.c#L2782 for functions used by lescan

# performs a simple device inquiry, and returns a list of ble advertizements 
# discovered device

# NOTE: Python's struct.pack() will add padding bytes unless you make the endianness explicit. Little endian
# should be used for BLE. Always start a struct.pack() format string with "<"

import sys
import time

import bluetooth._bluetooth as bluez
import struct

LE_META_EVENT = 0x3e
LE_PUBLIC_ADDRESS = 0x00
LE_RANDOM_ADDRESS = 0x01
LE_SET_SCAN_PARAMETERS_CP_SIZE = 7
OGF_LE_CTL = 0x08
OCF_LE_SET_SCAN_PARAMETERS = 0x000B
OCF_LE_SET_SCAN_ENABLE = 0x000C
OCF_LE_CREATE_CONN = 0x000D

LE_ROLE_MASTER = 0x00
LE_ROLE_SLAVE = 0x01

# these are actually subevents of LE_META_EVENT
EVT_LE_CONN_COMPLETE = 0x01
EVT_LE_ADVERTISING_REPORT = 0x02
EVT_LE_CONN_UPDATE_COMPLETE = 0x03
EVT_LE_READ_REMOTE_USED_FEATURES_COMPLETE = 0x04

# Advertisment event types
ADV_IND = 0x00
ADV_DIRECT_IND = 0x01
ADV_SCAN_IND = 0x02
ADV_NONCONN_IND = 0x03
ADV_SCAN_RSP = 0x04


def returnnumberpacket(pkt):
    myInteger = 0
    multiple = 256
    for c in pkt:
        myInteger += struct.unpack("B", c)[0] * multiple
        multiple = 1
    return myInteger


def returnstringpacket(pkt):
    myString = "";
    for c in pkt:
        myString += "%02x" % struct.unpack("B", c)[0]
    return myString


def printpacket(pkt):
    for c in pkt:
        sys.stdout.write("%02x " % struct.unpack("B", c)[0])


def get_packed_bdaddr(bdaddr_string):
    packable_addr = []
    addr = bdaddr_string.split(':')
    addr.reverse()
    for b in addr:
        packable_addr.append(int(b, 16))
    return struct.pack("<BBBBBB", *packable_addr)


def packed_bdaddr_to_string(bdaddr_packed):
    return ':'.join('%02x' % i for i in struct.unpack("<BBBBBB", bdaddr_packed[::-1]))


def hci_enable_le_scan(sock):
    hci_toggle_le_scan(sock, 0x01)


def hci_disable_le_scan(sock):
    hci_toggle_le_scan(sock, 0x00)


def hci_toggle_le_scan(sock, enable):
    cmd_pkt = struct.pack("<BB", enable, 0x00)
    bluez.hci_send_cmd(sock, OGF_LE_CTL, OCF_LE_SET_SCAN_ENABLE, cmd_pkt)


#    pkt = sock.recv(255)
##    print "socked recieved"
#    status,mode = struct.unpack("xxxxxxBB", pkt)
#    print status


def hci_le_set_scan_parameters(sock):
    # print "setting up scan"
    # old_filter = sock.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)
    # print "got old filter"

    SCAN_RANDOM = 0x01
    OWN_TYPE = SCAN_RANDOM
    SCAN_TYPE = 0x01
    INTERVAL = 0x10
    WINDOW = 0x10
    FILTER = 0x00  # all advertisements, not just whitelisted devices
    # interval and window are uint_16, so we pad them with 0x0
    cmd_pkt = struct.pack("<BBBBBBB", SCAN_TYPE, 0x0, INTERVAL, 0x0, WINDOW, OWN_TYPE, FILTER)
    # print "packed up: \"", str( cmd_pkt ) , "\""
    bluez.hci_send_cmd(sock, OGF_LE_CTL, OCF_LE_SET_SCAN_PARAMETERS, cmd_pkt)


def prepareScan(sock):
    return sock.getsockopt(bluez.SOL_HCI, bluez.HCI_FILTER, 14)


def finalizeScan(sock, old_filter):
    sock.setsockopt(bluez.SOL_HCI, bluez.HCI_FILTER, old_filter)


def clearDiscoveredDevices(sock):
    # perform a device inquiry on bluetooth device #0
    # The inquiry should last 8 * 1.28 = 10.24 seconds
    # before the inquiry is performed, bluez should flush its cache of
    # previously discovered devices
    flt = bluez.hci_filter_new()
    bluez.hci_filter_all_events(flt)
    bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
    sock.setsockopt(bluez.SOL_HCI, bluez.HCI_FILTER, flt)


def scanBeacons(sock, timeout):
    clearDiscoveredDevices(sock)

    myFullList = {}

    start = time.time()
    i = 0

    while True:

        try:
            # 0x40 is non blocking
            pkt = sock.recv(255, 0x40)

            # print pkt

            ptype, event, plen = struct.unpack("BBB", pkt[:3])
            if event == bluez.EVT_INQUIRY_RESULT_WITH_RSSI:
                i = 0
            elif event == bluez.EVT_NUM_COMP_PKTS:
                i = 0
            elif event == bluez.EVT_DISCONN_COMPLETE:
                i = 0
            elif event == LE_META_EVENT:
                subevent, = struct.unpack("B", pkt[3])
                pkt = pkt[4:]
                if subevent == EVT_LE_CONN_COMPLETE:
                    le_handle_connection_complete(pkt)
                elif subevent == EVT_LE_ADVERTISING_REPORT:
                    num_reports = struct.unpack("B", pkt[0])[0]
                    report_pkt_offset = 0
                    for i in range(0, num_reports):
                        mac = packed_bdaddr_to_string(pkt[report_pkt_offset + 3:report_pkt_offset + 9])
                        uuid = returnstringpacket(pkt[report_pkt_offset - 22: report_pkt_offset - 6])
                        major = "%i" % returnnumberpacket(pkt[report_pkt_offset - 6: report_pkt_offset - 4])
                        minor = "%i" % returnnumberpacket(pkt[report_pkt_offset - 4: report_pkt_offset - 2])
                        txpower = struct.unpack("b", pkt[report_pkt_offset - 2])
                        rssi = struct.unpack("b", pkt[report_pkt_offset - 1])

                        if not uuid in myFullList:
                            myFullList[uuid] = {"mac": mac, "uuid": uuid, "major": major, "minor": minor, "samples": []}

                        myFullList[uuid]['samples'].append(
                            {"txpower": txpower[0], "rssi": rssi[0], "timestamp": time.time()})

        except bluez.error as e:
            time.sleep(0.1)

        end = time.time()
        if (end - start) >= timeout:
            break

    return myFullList

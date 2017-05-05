#!/usr/bin/python

"""
Usage: sudo ibeacon [-u|--uuid=UUID or `random' (default=Beacon Toolkit app)]
                    [-M|--major=major (0-65535, default=0)]
                    [-m|--minor=minor (0-65535, default=0)]
                    [-p|--power=power (0-255, default=200)]
                    [-d|--device=BLE device to use (default=hci0)]
                    [-z|--down]
                    [-v|--verbose]
                    [-n|--simulate (implies -v)]
                    [-h|--help]

The default UUID matches that which the "Beacon Toolkit" app uses.
https://itunes.apple.com/us/app/beacon-toolkit/id728479775

UUID, major, minor and power may also be specified by setting the environment
variables `IBEACON_UUID,' `IBEACON_MAJOR,' `IBEACON_MINOR' and `IBEACON_POWER.'
Options set with command line switches always override defaults or environment.

NOTE: for environment variables to work, the following must be present
in your `sudoers' file:
  Defaults env_keep += "IBEACON_UUID IBEACON_MAJOR IBEACON_MINOR IBEACON_POWER"
"""

import os
import subprocess
import sys
import re
import getopt
import uuid

# option flags
simulate = False
verbose = False

# prints usage information
class Usage(Exception):
  def __init__(self, msg):
    self.msg = msg

# return a random UUID
def get_random_uuid():
  return uuid.uuid4().hex

# convert an integer into a hex value of a given number of digits
def hexify(i, digits=2):
  format_string = "0%dx" % digits
  return format(i, format_string).upper()

# swap the byte order of a 16-bit hex value
# NOT USED, leaving this here just in case, turns out major/minor ids
# are big-endian (i.e. no swap required)
def endian_swap(hex):
  hexbyte1 = hex[0] + hex[1]
  hexbyte2 = hex[2] + hex[3]
  newhex = hexbyte2 + hexbyte1
  return newhex

# split a hex string into 8-bit/2-hex-character groupings separated by spaces
def hexsplit(string):
  return ' '.join([string[i:i+2] for i in range(0, len(string), 2)])

# process a command string - print it if verbose is on,
# and if not simulating, then actually run the command
def process_command(c):
  global verbose
  if verbose:
    print ">>> %s" % c
  if not simulate:
    os.system(c)

# check to see if we are the superuser - returns 1 if yes, 0 if no
def check_for_sudo():
  if 'SUDO_UID' in os.environ.keys():
    return 1
  else:
    print "Error: this script requires superuser privileges.  Please re-run with `sudo.'"
    return 0

# check to see if the hci device is valid
# kind of a cheaty way of doing this, we just grep the output of
# `hcitool list' to make sure the passed-in device string is present
def is_valid_device(device):
  return not os.system("hciconfig list 2>/dev/null | grep -q ^%s:" % device)

###############################################################################

def main(argv=None):

  # option flags
  global verbose
  global simulate

  # default uuid, this is the uuid that the "Beacon Toolkit" iOS app uses
  # https://itunes.apple.com/us/app/beacon-toolkit/id728479775
  # can be overriden with environment variable
  uuid = os.getenv("IBEACON_UUID", "E20A39F473F54BC4A12F17D1AD07A961")

  # major and minor ids, can be overriden with environment variable
  major = int(os.getenv("IBEACON_MAJOR", "0"))
  minor = int(os.getenv("IBEACON_MINOR", "0"))

  # default to the first available bluetooth device
  device = "hci0"

  # default power level
  power = int(os.getenv("IBEACON_POWER", "200"))

  # regexp to test for a valid UUID
  # here the - separators are optional
  valid_uuid_match = re.compile('^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}$', re.I)

  # grab command line arguments
  if argv is None:
    argv = sys.argv

  # parse command line options
  try:
    try:
      opts, args = getopt.getopt(argv[1:], "hu:M:m:p:vd:nz", ["help", "uuid=", "major=", "minor=", "power=", "verbose", "device=", "simulate", "down"])

    except getopt.error, msg:
      raise Usage(msg)

    for o, a in opts:
      if o in ("-h", "--help"):
        print __doc__
        return 0
      elif o in ("-n", "--simulate"):
        simulate = True
        verbose = True
      elif o in ("-u", "--uuid"):
        uuid = a
        if uuid == "random":
          uuid = get_random_uuid()
      elif o in ("-M", "--major"):
        major = int(a)
      elif o in ("-m", "--minor"):
        minor = int(a)
      elif o in ("-p", "--power"):
        power = int(a)
      elif o in ("-v", "--verbose"):
        verbose = True
      elif o in ( "-d", "--device"):
        temp_device = str(a)
        # devices can be specified as "X" or "hciX"
        if not temp_device.startswith("hci"):
          device = "hci%s" % temp_device
        else:
          device = temp_device
      elif o in ( "-z", "--down"):
        if check_for_sudo():
          if is_valid_device(device):
            print "Downing iBeacon on %s" % device
            process_command("hciconfig %s noleadv" % device)
            #process_command("hciconfig %s piscan" % device)
            process_command("hciconfig %s down" % device)
            return 0
          else:
            print "Error: no such device: %s (try `hciconfig list')" % device
            return 1
        else:
          return 1

    # test for valid UUID
    if not valid_uuid_match.match(uuid):
      print "Error: `%s' is an invalid UUID." % uuid
      return 1

    # strip out - symbols from uuid and turn it into uppercase
    uuid = uuid.replace("-","")
    uuid = uuid.upper()

    # bounds check major/minor ids
    if major < 0 or major > 65535:
      print "Error: major id is out of bounds (0-65535)"
      return 1
    if minor < 0 or minor > 65535:
      print "Error: minor id is out of bounds (0-65535)"
      return 1

    # bail if we're not running as superuser (don't care if we are simulating)
    if not simulate and not check_for_sudo():
      return 1

    # split the uuid into 8 bit (=2 hex digit) chunks
    split_uuid = hexsplit(uuid)

    # convert major/minor id into hex
    major_hex = hexify(major, 4)
    minor_hex = hexify(minor, 4)

    # create split versions of these (for the hcitool command)
    split_major_hex = hexsplit(major_hex)
    split_minor_hex = hexsplit(minor_hex)

    # convert power into hex
    power_hex = hexify(power, 2)

    # make sure we are using a valid hci device
    if not simulate and not is_valid_device(device):
      print "Error: no such device: %s (try `hciconfig list')" % device
      return 1
 
    # print status info
    print "Advertising on %s with:" % device
    print "       uuid: 0x%s" % uuid
    print "major/minor: %d/%d (0x%s/0x%s)" % (major, minor, major_hex, minor_hex)
    print "      power: %d (0x%s)" % (power, power_hex)

    # first bring up bluetooth
    process_command("hciconfig %s up" % device)
    # now turn on LE advertising (3 => non-connectable advertisement)
    
    # now turn off scanning
    process_command("hciconfig %s noscan" % device)

    # set up the beacon
    # 1E Number of bytes that follow in the advertisement
    # 02 Number of bytes that follow in first AD structure
    # 01 Flags AD type
    # 1A Flags value 0x1A = 000011010  
    #   bit 0 (OFF) LE Limited Discoverable Mode
    #   bit 1 (ON) LE General Discoverable Mode
    #   bit 2 (OFF) BR/EDR Not Supported
    #   bit 3 (ON) Simultaneous LE and BR/EDR to Same Device Capable (controller)
    #   bit 4 (ON) Simultaneous LE and BR/EDR to Same Device Capable (Host)
    # 1A Number of bytes that follow in second (and last) AD structure
    # FF Manufacturer specific data AD type
    # 4C Company identifier code LSB
    # 00 Company identifier code MSB (0x004C == Apple)
    # 02 Byte 0 of iBeacon advertisement indicator
    # 15 Byte 1 of iBeacon advertisement indicator
    process_command("hcitool -i %s cmd 0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 %s %s %s %s 00 >/dev/null" % (device,split_uuid, split_major_hex, split_minor_hex, power_hex))
 
    # https://esf.eurotech.com/docs/how-to-user-bluetooth-le-beacons
    # http://stackoverflow.com/questions/21124993/is-there-a-way-to-increase-ble-advertisement-frequency-in-bluez
    # 0-3  A0 00  min interval - A0 00 => 100ms => (100ms = 0x0A * 0.625ms), 40 06 => (1000ms = 0x0640 * 0.625ms)
    # 4-7  A0 00  max interval - A0 00 => 100ms => (100ms = 0x0A * 0.625ms), 40 06 => (1000ms = 0x0640 * 0.625ms)
    # 8-11 03 00  leadv 3
    #                                              0     4     8     12    16    20    24
    process_command("hcitool -i %s cmd 0x08 0x0006 A0 00 A0 00 03 00 00 00 00 00 00 00 00 07 00" % device)

    # (0x08 0x000a) is "LE Set Advertise Enable"
    process_command("hcitool -i %s cmd 0x08 0x000a 01" % device)
    #process_command("hciconfig %s leadv 3" % device)

  except Usage, err:
    print >>sys.stderr, err.msg
    print >>sys.stderr, "for help use --help"
    return 2

###############################################################################

if __name__ == "__main__":
  sys.exit(main())
 

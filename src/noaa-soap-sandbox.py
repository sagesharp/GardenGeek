#!/usr/bin/python
#
# Copyright (C) 2010 Sarah Sharp
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software Foundation,
# Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
#
#
# Note: This uses a sample function from the Python documentation of minidom,
# getText().  The documentation, and its examples, are released under a
# GPL-compatible license, according to http://docs.python.org/license.html

from suds.client import Client
from datetime import *
from xml.dom.minidom import parse, parseString
import sys
from optparse import OptionParser

def getText(nodelist):
	rc = []
	for node in nodelist:
		if node.nodeType == node.TEXT_NODE:
			rc.append(node.data)
	return ''.join(rc)

def findTemp(nodelist):
	for node in nodelist:
		values = node.getElementsByTagName("value")
		temperature = getText(values[0].childNodes)
		return int(temperature)

def getTempXml(zipcode):
	url = 'http://www.weather.gov/forecasts/xml/SOAP_server/ndfdXMLserver.php?wsdl'
	client = Client(url)
	latlong_xml = client.service.LatLonListZipCode(zipcode)

	dom = parseString(latlong_xml)
	latlong = getText(dom.getElementsByTagName("latLonList")[0].childNodes)
	latitude = float(latlong.split(',')[0])
	longitude = float(latlong.split(',')[1])

	requests = client.factory.create('weatherParametersType')
	requests.mint = 'TRUE'
	mintemp = client.service.NDFDgen(latitude, longitude, 'glance',
			datetime.now(), datetime.now(), requests)
	dom2 = parseString(mintemp)
	temps = dom2.getElementsByTagName("temperature")
	temperature = findTemp(temps)
	print temperature

def printUsage():
	print "Usage: noaa-soap-sandbox.py zipcode"
	print "   zipcode: 5-digit United States postal code"

if __name__ == "__main__":
	parser = OptionParser()
	(options, args) = parser.parse_args()
	if len(args) != 1 or not args[0].isdigit() or len(args[0]) != 5:
		printUsage()
		sys.exit()
	getTempXml(args[0])

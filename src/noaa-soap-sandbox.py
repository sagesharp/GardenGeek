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

from suds.client import Client
from datetime import *
import xml.parsers.expat

def print_latlong(data):
	print ' ', repr(data)

def sandbox():
	url = 'http://www.weather.gov/forecasts/xml/SOAP_server/ndfdXMLserver.php?wsdl'
	client = Client(url)
	print client
	latlong = client.service.LatLonListZipCode('00000')
	print latlong

	# FIXME grab the lat
	p = xml.parsers.expat.ParserCreate()
	p.CharacterDataHandler = print_latlong
	p.Parse(latlong)

	requests = client.factory.create('weatherParametersType')
	requests.mint = 'TRUE'
	print requests
	mintemp = client.service.NDFDgen(40, -120, 'glance',
			datetime.now(), datetime.now(), requests)
	print mintemp

if __name__ == "__main__":
	sandbox()

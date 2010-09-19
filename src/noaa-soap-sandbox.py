#!/usr/bin/python
from suds.client import Client
from datetime import *

def sandbox():
	url = 'http://www.weather.gov/forecasts/xml/SOAP_server/ndfdXMLserver.php?wsdl'
	client = Client(url)
	print client
	latlong = client.service.LatLonListZipCode('00000')
	print latlong
	# FIXME grab the lat
	requests = client.factory.create('weatherParametersType')
	requests.mint = 'TRUE'
	print requests
	mintemp = client.service.NDFDgen(40, -120, 'glance',
			datetime.now(), datetime.now(), requests)
	print mintemp

if __name__ == "__main__":
	sandbox()

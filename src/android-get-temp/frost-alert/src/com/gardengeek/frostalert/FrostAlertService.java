package com.gardengeek.frostalert;

import java.io.StringReader;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.*;
import org.ksoap2.transport.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class FrostAlertService {
	/* zipcodes can have leading zeros */
	private Integer zipcode;
	/* NOAA.gov SOAP constants */
	private static final String ZIP_SOAP_ACTION = "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl#LatLonListZipCode";
	private static final String ZIP_METHOD_NAME = "LatLonListZipCode";
	private static final String NOAA_NAMESPACE = "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl";
	private static final String NOAA_URL = "http://www.weather.gov/forecasts/xml/SOAP_server/ndfdXMLserver.php";
	
	public String getZipcode()
	{
		return String.format("%05d", zipcode);
	}
	public void setZipcode(Integer zip)
	{
		zipcode = zip;
	}
	private String parseLatLong(String xml)
	{
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			Element element = (Element) doc.getElementsByTagName("latLonList").item(0);
			return element.getFirstChild().getNodeValue();
		} catch(Exception e){
			return e.toString();
		}
	}
	public String getLatLong()
	{
		try {
			SoapObject request = new SoapObject(NOAA_NAMESPACE, ZIP_METHOD_NAME);
			request.addProperty("LatLonListZipCodeRequest", this.getZipcode());

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(NOAA_URL);
			androidHttpTransport.call(ZIP_SOAP_ACTION, envelope);

			/*
			 * envelope.getResult() doesn't work here, although it's in all the tutorials.
			 * I get a java class exception "java.lang.String".
			 */
			SoapObject result = (SoapObject) envelope.bodyIn;

			/*
			 * Ugh, no idea why this isn't parsing the properties more.
			 * Why I can't I just say result.getPropery("latLonList")?
			 * Grab the resulting xml instead, and parse it.
			 */
			Object xml = result.getProperty("listLatLonOut");
			return parseLatLong(xml.toString());
		} catch (Exception e) {
			return "exception";
		}
	}
}

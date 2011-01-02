package com.gardengeek.frostalert;

import java.io.StringReader;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.*;
import org.ksoap2.transport.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class FrostAlertService {
	/* zipcodes can have leading zeros */
	private Integer zipcode;
	private Integer minimumTemp;
	/* NOAA.gov SOAP constants */
	private static final String ZIP_SOAP_ACTION = "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl#LatLonListZipCode";
	private static final String ZIP_METHOD_NAME = "LatLonListZipCode";
	private static final String WEATHER_SOAP_ACTION = "http://www.weather.gov/forecasts/xml/DWMLgen/wsdl/ndfdXML.wsdl#NDFDgen";
	private static final String WEATHER_METHOD_NAME = "NDFDgen";
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
	public Integer getMinimumTemperature()
	{
		return minimumTemp;
	}
	public void setMinimumTemperature(Integer minimumTemperature)
	{
		minimumTemp = minimumTemperature;
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
	private String parseMinimumTemperature(String xml)
	{
		int maxItems;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			NodeList tempList = doc.getElementsByTagName("temperature");
			maxItems = tempList.getLength();
			for (int i = 0; i < maxItems; i++)
			{
				Element item = (Element) tempList.item(i);
				if (item.getAttribute("type").equals("minimum"))
				{
					return item.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
				}
			}
			return "-400";
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
	public String getTempForNextDay(String latlong, int daysInFuture)
	{
		try {
			/* xsd:DateTime format is [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm] */
			String dateTimeFormat = "yyyy-MM-dd";
			String midnight = "T00:00:00";
			String endOfDay = "T23:59:59";
			String date;
			SoapObject request;
			SoapObject mint;
			Calendar cal;
			SimpleDateFormat sdf;
			
			request = new SoapObject(NOAA_NAMESPACE, WEATHER_METHOD_NAME);
			request.addProperty("latitude", latlong.split(",")[0]);
			request.addProperty("longitude", latlong.split(",")[1]);
			request.addProperty("product", "glance");
			
			cal = Calendar.getInstance();
			cal.add(Calendar.DATE, daysInFuture);
			sdf = new SimpleDateFormat(dateTimeFormat);
			date = new String(sdf.format(cal.getTime()) + midnight);
			request.addProperty("startDate", date);
			date = new String(sdf.format(cal.getTime()) + endOfDay);
			request.addProperty("endDate", date);
			/* only need mint, so use "glance" instead of "time-series" */
			
			mint = new SoapObject(NOAA_NAMESPACE, "weatherParametersType");
			mint.addProperty("mint", Boolean.TRUE);
			request.addProperty("weatherParameters", mint);

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet = false;
			envelope.setOutputSoapObject(request);
			HttpTransportSE androidHttpTransport = new HttpTransportSE(NOAA_URL);
			androidHttpTransport.call(WEATHER_SOAP_ACTION, envelope);

			/*
			 * Stupid NOAA doesn't explicitly define a format for their output.
			 * Instead the output is just "string" a.k.a. xml tag soup.
			 */
			SoapObject result = (SoapObject) envelope.bodyIn;
			String xml = result.getProperty(0).toString();
			return parseMinimumTemperature(xml);
		} catch (Exception e) {
			return "exception";
		}
	}
}

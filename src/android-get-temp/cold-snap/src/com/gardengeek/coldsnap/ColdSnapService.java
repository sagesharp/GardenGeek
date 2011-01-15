package com.gardengeek.coldsnap;

import java.io.StringReader;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.*;
import org.ksoap2.transport.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.text.SimpleDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ColdSnapService {
	
	public static class DateTemp {
		public final String date;
		public final String temp;
		public DateTemp(String date, String temp)
		{
			this.date = date;
			this.temp = temp;
		}
	}
	/* zipcodes can have leading zeros */
	private Integer zipcode;
	private Integer coldTemp;
	private List<String> minimumTemps;
	private String validDateID;
	private List<String> dayNames;
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
	public Integer getColdTemperature()
	{
		return coldTemp;
	}
	public void setColdTemperature(Integer minimumTemperature)
	{
		coldTemp = minimumTemperature;
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
	private List<String> parseMinimumTemperature(Document doc)
	{
		int maxItems;
		List<String> minTemps = new ArrayList<String>();
		try {
			NodeList tempList = doc.getElementsByTagName("temperature");
			maxItems = tempList.getLength();
			for (int i = 0; i < maxItems; i++)
			{
				Element item = (Element) tempList.item(i);
				if (!(item instanceof Element))
					continue;
				if (item.getAttribute("type").equals("minimum"))
				{
					validDateID = item.getAttribute("time-layout");
					NodeList minimumTemps = item.getElementsByTagName("value");
					for (int j = 0; j < minimumTemps.getLength(); j++)
						minTemps.add(minimumTemps.item(j).getFirstChild().getNodeValue());
				}
			}
			return minTemps;
		} catch(Exception e){
			return Collections.singletonList(e.toString());
		}
	}
	private List<String> parseDays(Document doc)
	{
		int maxItems;
		List<String> dayStrings = new ArrayList<String>();
		try {
			NodeList timeLayoutList = doc.getElementsByTagName("time-layout");
			maxItems = timeLayoutList.getLength();
			for (int i = 0; i < maxItems; i++)
			{
				Element item = (Element) timeLayoutList.item(i);
				if (!(item instanceof Element))
					continue;
				Element itemNested = (Element) item.getElementsByTagName("layout-key").item(0);
				if (itemNested.getFirstChild().getNodeValue().equals(validDateID)) {
					NodeList validTimeList = item.getElementsByTagName("start-valid-time");
					for (int j = 0; j < validTimeList.getLength(); j++) {
						Element validTime = (Element) validTimeList.item(j);
						dayStrings.add(validTime.getAttribute("period-name"));
					}
				}
			}
			return dayStrings;
		} catch(Exception e){
			return Collections.singletonList(e.toString());
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
	/* Gets a list of the minimum temperatures over the next three days */
	public List<DateTemp> getMinimumTemperatures(String latlong)
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
			sdf = new SimpleDateFormat(dateTimeFormat);
			date = new String(sdf.format(cal.getTime()) + midnight);
			request.addProperty("startDate", date);
			cal.add(Calendar.DATE, 3);
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
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			minimumTemps = parseMinimumTemperature(doc);
			dayNames = parseDays(doc);
			List<DateTemp> list = new ArrayList<DateTemp>();
			for (int i = 0; i < minimumTemps.size(); i++)
				list.add(new DateTemp(dayNames.get(i), minimumTemps.get(i)));
			return list;
		} catch (Exception e) {
			return Collections.singletonList(new DateTemp("Never", "-400"));
		}
	}
}

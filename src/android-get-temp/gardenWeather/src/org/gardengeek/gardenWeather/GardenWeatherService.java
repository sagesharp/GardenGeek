package org.gardengeek.gardenWeather;

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

public class GardenWeatherService {
	private int NUMBERDAYS = 3;
	
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
	private Integer zipcode = Integer.MIN_VALUE;
	private Integer coldTemp = Integer.MIN_VALUE;
	private String latlong;
	private List<String> minimumTemps;
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
		if (zipcode == Integer.MIN_VALUE)
			return null;
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
	public void fetchLatLong() throws Exception
	{
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
		latlong = parseLatLong(xml.toString());
	}
	public String getLatLong()
	{
		return latlong;
	}
	/* Gets a list of the minimum temperatures over the next three days.
	 * If the latitude and longitude have not been fetched, it tries to fetch it.
	 * If that fails, or getting the minimum temperatures fails,
	 * then it will throw an exception.
	 */
	public List<DateTemp> getMinimumTemperatures() throws Exception
	{
		/* xsd:DateTime format is [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm] */
		String dateTimeFormat = "yyyy-MM-dd";
		String dayFormat = "EEE";
		String midnight = "T00:00:00";
		String endOfDay = "T23:59:59";
		String date;
		SoapObject request;
		SoapObject mint;
		Calendar cal;
		SimpleDateFormat sdf;
		SimpleDateFormat sdfDay;

		if (latlong == null)
			fetchLatLong();
		
		request = new SoapObject(NOAA_NAMESPACE, WEATHER_METHOD_NAME);
		request.addProperty("latitude", latlong.split(",")[0]);
		request.addProperty("longitude", latlong.split(",")[1]);
		request.addProperty("product", "glance");

		dayNames = new ArrayList<String>();
		cal = Calendar.getInstance();
		sdf = new SimpleDateFormat(dateTimeFormat);
		sdfDay = new SimpleDateFormat(dayFormat);
		date = new String(sdf.format(cal.getTime()) + midnight);
		request.addProperty("startDate", date);

		dayNames.add(new String("Today"));
		cal.add(Calendar.DATE, 1);
		dayNames.add(new String(sdfDay.format(cal.getTime())));
		cal.add(Calendar.DATE, 1);
		dayNames.add(new String(sdfDay.format(cal.getTime())));

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
		List<DateTemp> list = new ArrayList<DateTemp>();
		for (int i = 0; i < NUMBERDAYS && i < minimumTemps.size(); i++)
			list.add(new DateTemp(dayNames.get(i), minimumTemps.get(i)));
		return list;
	}
}

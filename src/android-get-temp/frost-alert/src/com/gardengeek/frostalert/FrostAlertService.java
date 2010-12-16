package com.gardengeek.frostalert;

public class FrostAlertService {
	/* zipcodes can have leading zeros */
	private Integer zipcode;
	
	public String getZipcode()
	{
		return String.format("%05d", zipcode);
	}
	public void setZipcode(Integer zip)
	{
		zipcode = zip;
	}
}

package com.jd.fc.model;

import com.google.gson.annotations.SerializedName;

public class CountriesItem{

	@SerializedName("country_name")
	public String countryName;

	@SerializedName("id")
	public int id;

	public boolean isSelected;
}
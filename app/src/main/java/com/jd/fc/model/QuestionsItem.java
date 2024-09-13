package com.jd.fc.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class QuestionsItem{

	@SerializedName("country_code")
	public String countryCode;

	@SerializedName("countries")
	public List<CountriesItem> countries;

	@SerializedName("answer_id")
	public int answerId;
}
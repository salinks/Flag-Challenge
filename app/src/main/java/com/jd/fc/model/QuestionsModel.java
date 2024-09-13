package com.jd.fc.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class QuestionsModel{

	@SerializedName("questions")
	public List<QuestionsItem> questions;
}
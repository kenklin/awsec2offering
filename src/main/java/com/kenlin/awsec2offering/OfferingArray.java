package com.kenlin.awsec2offering;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class OfferingArray {
	@JsonProperty public List<Offering> ec2offerings = new ArrayList<Offering>();
	
	public OfferingArray() {}
}

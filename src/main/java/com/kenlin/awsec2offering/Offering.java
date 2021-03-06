/*
 * Copyright 2014 Ken K. Lin. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenlin.awsec2offering;

import java.io.IOException;

import com.amazonaws.services.ec2.model.ReservedInstancesOffering;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @todo Revisit the JSON schema by comparing the current output to
 * aws ec2 describe-reserved-instances-offerings
 *		--no-include-marketplace
 *		--availability-zone us-east-1a
 *		--product-description "Linux/UNIX"
 *		--offering-type "heavy utilization"
 *		--instance-type t1.micro
 */
@JsonAutoDetect
public class Offering {
	public final static String	AVAILABILITYZONE	= "availabilityZone";
	public final static String	OFFERINGTYPE		= "offeringType";
	public final static String	INSTANCETYPE		= "instanceType";
	public final static String	PRODUCTDESCRIPTION	= "productDescription";
	public final static String	DURATION			= "duration";
	public final static String	CURRENCYCODE		= "currencyCode";
	public final static String	FIXEDPRICE			= "fixedPrice";
	public final static String	HOURLYPRICE			= "hourlyPrice";
	
	public final static long	SECONDS_IN_YEAR		= 31536000;
	public final static long	SECONDS_IN_3_YEARS	= 94608000;
	public final static long	SECONDS_IN_MONTH	= SECONDS_IN_YEAR / 12;	// 2628000
	public final static int		HOURS_IN_MONTH		= 365 * 24 / 12; 		// 730

	private static ObjectMapper	mapper = new ObjectMapper();
	  
	@JsonProperty public String	availabilityZone = null;	// e.g., "us-east-1a"
	@JsonProperty public String	offeringType = null;		// e.g., "Heavy Utilization"
	@JsonProperty public String	instanceType = null;		// e.g., "m1.small"
	@JsonProperty public String	productDescription = null;	// e.g., "Linux/UNIX (Amazon VPC)"
	@JsonProperty public String	currencyCode = null;		// e.g., "USD"
	@JsonProperty public Long	duration = null;			// e.g., YEAR1 or YEAR3
	@JsonProperty public Float	fixedPrice = null;			// e.g., 169.0
	@JsonProperty public Double	hourlyPrice = null;			// e.g., 0.014
	
/* totalMonthlyCost functionality has been offloaded to the client-side

	private float totalMonthlyCost[] = null;	// Where [0..36]

	// Create and populate totalMonthlyCost[1 + 36].
	private static float[] calculateTotalCost(long duration, float fixedPrice, double hourlyPrice) {
		float totalMonthlyCost[] = null;
		if (fixedPrice > 0 && hourlyPrice > 0) {
			final double monthlyPrice = hourlyPrice * HOURS_IN_MONTH;
			long durationInMonths = duration / SECONDS_IN_MONTH;

			totalMonthlyCost = new float[1 + 36];
			totalMonthlyCost[0] = fixedPrice;
			for (int month = 1; month <= 36; month++) {
				long terms = ((month - 1) / durationInMonths) + 1;	// 1 year term 1 = months 0-12, 1 year term 2 = months 13-24
				totalMonthlyCost[month] = (float)(terms * fixedPrice);
				totalMonthlyCost[month] += (float)(month * monthlyPrice);
			}
		}
		return totalMonthlyCost;
	}
*/

	public Offering() {}
	
	public Offering(ReservedInstancesOffering offering) {
		availabilityZone	= offering.getAvailabilityZone();
		offeringType		= offering.getOfferingType();
		instanceType		= offering.getInstanceType();
		productDescription	= offering.getProductDescription();
		currencyCode		= offering.getCurrencyCode();
		duration			= offering.getDuration();
		fixedPrice			= offering.getFixedPrice();
		try {
			hourlyPrice		= offering.getRecurringCharges().get(0).getAmount();
		} catch (IndexOutOfBoundsException e) {
			hourlyPrice		= offering.getUsagePrice().doubleValue();	// e.g., c1.medium
		}
/* totalMonthlyCost functionality has been offloaded to the client-side			
		totalMonthlyCost	= calculateTotalCost(duration, fixedPrice, hourlyPrice);
*/
	}
	
	@Override
	public String toString() {
		try {
			JsonNode json = toJsonNode();
			return json.toString();
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	
	public JsonNode toJsonNode() throws JsonProcessingException, IOException {
		ObjectNode json = mapper.createObjectNode();
		if (availabilityZone != null)	json.put(AVAILABILITYZONE, availabilityZone);
		if (offeringType != null)		json.put(OFFERINGTYPE, offeringType);
		if (instanceType != null)		json.put(INSTANCETYPE, instanceType);
		if (productDescription != null)	json.put(PRODUCTDESCRIPTION, productDescription);
		if (duration != null)			json.put(DURATION, duration);
		if (currencyCode != null)		json.put(CURRENCYCODE, currencyCode);
		if (fixedPrice != null)			json.put(FIXEDPRICE, fixedPrice);
		if (hourlyPrice != null)		json.put(HOURLYPRICE, hourlyPrice);
		return json;
	}
}

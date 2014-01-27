package com.kenlin.awsec2offering;

import java.io.IOException;

import com.amazonaws.services.ec2.model.ReservedInstancesOffering;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	  
	private String	availabilityZone = null;	// e.g., "us-east-1a"
	private String	offeringType = null;		// e.g., "Heavy Utilization"
	private String	instanceType = null;		// e.g., "m1.small"
	private String	productDescription = null;	// e.g., "Linux/UNIX (Amazon VPC)"
	private String	currencyCode = null;		// e.g., "USD"
	private Long	duration = null;			// e.g., YEAR1 or YEAR3
	private Float	fixedPrice = null;			// e.g., 169.0
	private Double	hourlyPrice = null;			// e.g., 0.014
	
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

/**
 * Project creation:
 * 1) File / New Project / Maven / maven-archetype-quickstart
 * 2) Maven Dependencies
 * 		com.amazonaws aws-java-sdk 1.6.12
 * 		org.springframework spring-webmvc 3.2.6.RELEASE
 * 3) Properties / Project Facets /
 * 		Dynamic Web Module
 * 4) To resolve javax.servlet.*
 * 		<project> / Build Path / Configure Build Path / Add Library / Server Runtime / Tomcat
 * 5) Copy / modify in WebContent\WEB-INF ...
 * 		web.xml
 * 		awsec2offering-servlet.xml
 * 6) To make sure libs are in .war
 * 		<project> / Properties / Deployment Assembly / Java Build Path Entries / Maven Dependencies
 */
package com.kenlin.awsec2offering;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeReservedInstancesOfferingsRequest;
import com.amazonaws.services.ec2.model.DescribeReservedInstancesOfferingsResult;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.OfferingTypeValues;
import com.amazonaws.services.ec2.model.RIProductDescription;
import com.amazonaws.services.ec2.model.ReservedInstancesOffering;
import com.amazonaws.services.ec2.model.Tenancy;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
public class App {
	public static final String	ONDEMAND_URL ="https://raw2.github.com/kenklin/awsec2offering/master/src/main/resources/aws-ec2-ondemand.json";
	
	// URI components
	public static final String	LINUX_PREFIX = "linux";
	public static final String	WINDOWS_PREFIX = "windows";
	public static final String	AMAZONVPC_SUFFIX = "vpc";

	public static final String	HEAVY_PREFIX = "heavy";
	public static final String	MEDIUM_PREFIX = "medium";
	public static final String	LIGHT_PREFIX = "light";

	public static final String	AVAILABILITYZONE_DEFAULT = "us-east-1a";
	public static final String	PRODUCTDESCRIPTION_DEFAULT = LINUX_PREFIX;

	public static final String	SEPARATOR = ","; // Separates multi-value instanceType

	// JSON output
	public static final String	ARRAYNAME = "ec2offerings";

	// Instance data members
	private AmazonEC2Client		ec2 = null;
	private ObjectMapper		mapper = null;

	/**
	 * A URI-appropriate EC2 product description parser.
	 * 
	 * Note: The strings that RIProductDescription.fromValue accepts are not URI
	 * appropriate as they have spaces, etc.
	 * 
	 * @param value
	 * @return
	 */
	public static String normalizeRIProductDescription(String value) {
		if (value.startsWith(LINUX_PREFIX)) {
			return value.endsWith(AMAZONVPC_SUFFIX)
					? RIProductDescription.LinuxUNIXAmazonVPC.toString()
					: RIProductDescription.LinuxUNIX.toString();
		} else if (value.startsWith(WINDOWS_PREFIX)) {
			return value.endsWith(AMAZONVPC_SUFFIX)
					? RIProductDescription.WindowsAmazonVPC.toString()
					: RIProductDescription.Windows.toString();
		} else {
			return RIProductDescription.fromValue(value).toString();
		}
	}

	/**
	 * A URI-appropriate EC2 offering type parser.
	 * 
	 * Note: The strings that OfferingTypeValues.fromValue accepts are not URI
	 * appropriate as they have spaces, etc.
	 * 
	 * @param value
	 * @return
	 */
	public static String normalizeOfferingType(String value) {
		if (value.startsWith(HEAVY_PREFIX)) {
			return OfferingTypeValues.HeavyUtilization.toString();
		} else if (value.startsWith(MEDIUM_PREFIX)) {
			return OfferingTypeValues.MediumUtilization.toString();
		} else if (value.startsWith(LIGHT_PREFIX)) {
			return OfferingTypeValues.LightUtilization.toString();
		} else {
			return OfferingTypeValues.fromValue(value).toString();
		}
	}

	public static InstanceType parseInstanceType(String value) {
		return InstanceType.fromValue(value);
	}

	public static void addCORSHeaders(HttpServletResponse resp) {
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Methods", "GET");
		resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
	}

	public App() {
		// e.g., C:\apache-tomcat-7.0.42\bin\setenv.bat
		// set "JRE_HOME=%ProgramFiles%\Java\jre6"
		// set "JAVA_HOME=%ProgramFiles%\Java\jre6"
		// set "AWS_ACCESS_KEY_ID=xxx"
		// set "AWS_SECRET_KEY=xxxxxxxxx"
		// exit /b 01
		ec2 = new AmazonEC2Client(new EnvironmentVariableCredentialsProvider());
		mapper = new ObjectMapper();
	}

	private OfferingArray readOnDemandOfferings() throws JsonParseException, JsonMappingException, IOException {
		URL url = new URL(ONDEMAND_URL);
		return mapper.readValue(url, OfferingArray.class);
	}
	
	private void addOnDemandOfferings(ArrayNode array,
		String availabilityZone, String productDescription, String instanceType)
	{
		try {
			OfferingArray offerings = readOnDemandOfferings();
			for (Offering offering : offerings.ec2offerings) {
				if ((availabilityZone != null && !availabilityZone.equals(offering.availabilityZone))
					|| (productDescription != null && !productDescription.equals(offering.productDescription))
					|| (instanceType != null && !instanceType.equals(offering.instanceType)))
					continue;
				array.add(offering.toJsonNode());
			}
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addReservedOfferings(ArrayNode array,
			String availabilityZone, String productDescription, String offeringType, String instanceType)
	{
		DescribeReservedInstancesOfferingsRequest req = new DescribeReservedInstancesOfferingsRequest()
				.withIncludeMarketplace(false).
				withInstanceTenancy(Tenancy.Default); // Not Tenancy.Dedicated
		if (availabilityZone != null)
			req.setAvailabilityZone(availabilityZone);
		if (productDescription != null)
			req.setProductDescription(productDescription);
		if (offeringType != null)
			req.setOfferingType(offeringType);
		if (instanceType != null)
			req.setInstanceType(parseInstanceType(instanceType));

		String nextToken = null;
		do {
			DescribeReservedInstancesOfferingsResult res = ec2.describeReservedInstancesOfferings(req);
			for (ReservedInstancesOffering o : res.getReservedInstancesOfferings()) {
				Offering offering = new Offering(o);
				try {
					array.add(offering.toJsonNode());
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			nextToken = res.getNextToken();
			req.withNextToken(nextToken);
		} while (nextToken != null);
	}

	/**
	 * Constructs the JSON object with an array of abridged EC2 reserved
	 * instance offering objects.
	 * 
	 * @param availabilityZone
	 *            The AWS availability zone to limit the retrievals to.
	 * @param productDescription
	 *            The EC2 product descriptions to limit the retrievals to.
	 * @return
	 */
	private JsonNode getOfferingsAsJsonNode(String availabilityZone, String productDescription, String offeringType, String instanceType)
			throws JsonProcessingException, IOException
	{
		productDescription = normalizeRIProductDescription(productDescription);	// e.g., "linux" -> "Linux/UNIX"
		offeringType = normalizeOfferingType(offeringType);	// e.g., "heavy" -> "Heavy Utilization"
		
		ArrayNode array = mapper.createArrayNode();
		if (instanceType == null) {
			try {
				addOnDemandOfferings(array, availabilityZone, productDescription, instanceType);
				addReservedOfferings(array, availabilityZone, productDescription, offeringType, instanceType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// Google
			// https://www.google.com/search?q=java+spring+pathvariable+encode
			// http://stackoverflow.com/questions/9608711/spring-mvc-path-variables-encoding
			for (String str : instanceType.split(SEPARATOR)) {
				try {
					addOnDemandOfferings(array, availabilityZone, productDescription, str);
					addReservedOfferings(array, availabilityZone, productDescription, offeringType, str);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		ObjectNode root = mapper.createObjectNode();
		root.put(ARRAYNAME, array);
		return root;
	}

	// e.g., http://localhost:8080/awsec2offering/awsec2offering/api/splat
	// @see http://docs.spring.io/spring/docs/3.2.4.RELEASE/spring-framework-reference/html/mvc.html#mvc-config
	// @see https://gist.github.com/kdonald/2012289/raw/363289ee8652823f770ef82f594e9a8f15048090/ExampleController.java
	@RequestMapping(value = "/awsec2offering/api/{availabilityZone}/{productDescription}/{offeringType}/{instanceType}", method = RequestMethod.GET)
	@ResponseBody
	public JsonNode getOfferings(@PathVariable String availabilityZone,
			@PathVariable String productDescription,
			@PathVariable String offeringType,
			@PathVariable String instanceType, HttpServletRequest req,
			HttpServletResponse resp) {
		JsonNode json = null;
		try {
			addCORSHeaders(resp);

			App app = new App();
			json = app.getOfferingsAsJsonNode(availabilityZone,
					productDescription, offeringType, instanceType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

	@RequestMapping(value = "/awsec2offering/api/{availabilityZone}/{productDescription}/{offeringType}", method = RequestMethod.GET)
	@ResponseBody
	public JsonNode getOfferings(@PathVariable String availabilityZone, @PathVariable String productDescription, @PathVariable String offeringType,
			HttpServletRequest req, HttpServletResponse resp)
	{
		return getOfferings(availabilityZone, productDescription, offeringType, null, req, resp);
	}

	@RequestMapping(value = "/awsec2offering/api/{availabilityZone}/{productDescription}", method = RequestMethod.GET)
	@ResponseBody
	public JsonNode getOfferings(@PathVariable String availabilityZone, @PathVariable String productDescription,
			HttpServletRequest req, HttpServletResponse resp)
	{
		return getOfferings(availabilityZone, productDescription, null, null, req, resp);
	}

	@RequestMapping(value = "/awsec2offering/api/{availabilityZone}", method = RequestMethod.GET)
	@ResponseBody
	public JsonNode getOfferings(@PathVariable String availabilityZone,
			HttpServletRequest req, HttpServletResponse resp)
	{
		return getOfferings(availabilityZone, null, null, null, req, resp);
	}

	@RequestMapping(value = "/awsec2offering/api/", method = RequestMethod.GET)
	@ResponseBody
	public JsonNode getAllOfferings(HttpServletRequest req, HttpServletResponse resp)
	{
		return getOfferings(AVAILABILITYZONE_DEFAULT, PRODUCTDESCRIPTION_DEFAULT, null, null, req, resp);
	}

	public static void main(String[] args) throws Exception {
		String availabilityZone = AVAILABILITYZONE_DEFAULT;
		String productDescription = PRODUCTDESCRIPTION_DEFAULT;
		String offeringType = HEAVY_PREFIX;
		String instanceType = "t1.micro,m1.small";

		App app = new App();
		try {
			JsonNode json = app.getOfferingsAsJsonNode(availabilityZone, productDescription, offeringType, instanceType);
			System.out.println(json);
		} catch (AmazonServiceException ase) {
			ase.printStackTrace();
		} catch (AmazonClientException ace) {
			ace.printStackTrace();
		}
	}
}

AWS EC2 Offering Description Web Service (awsec2offering)
=========================================================
Software vendors using Amazon Web Service's Elastic Compute Cloud are faced with understanding different EC2 offerings and their [costs](http://aws.amazon.com/ec2/purchasing-options/reserved-instances/).

This RESTful web service returns both AWS EC2 on-demand and reserved instance offering descriptions which include both fixed and monthly costs.  Marketplace and spot instance offerings are excluded.  It obtains its information in the following manner ...
- *Reserved instance* offering information is obtained from AWS via [DescribeReservedInstanceOfferings](http://docs.aws.amazon.com/AWSEC2/latest/APIReference/ApiReference-query-DescribeReservedInstancesOfferings.html) calls.  While information is cached on the awsec2offering server, cached results are purged every 24 hours.
- *On-demand* instance offering information is not available via the AWS SDK.  Therefore it has been hand transcribed from the AWS EC2 Pricing [page](http://aws.amazon.com/ec2/pricing/) into a JSON [file](https://github.com/kenklin/awsec2offering/blob/master/src/main/resources/aws-ec2-ondemand.json) on GitHub which the awsec2offering server accesses over the internet.  This information is also purged every 24 hours.  If you notice  price changes, please contribute by ...
    - Posting it as an [issue](https://github.com/kenklin/awsec2offering/issues), or better yet,
    - Modifying  [aws-ec2-ondemand.json](https://github.com/kenklin/awsec2offering/blob/master/src/main/resources/aws-ec2-ondemand.json) and send a pull request.


Request
-------
The URI used to request EC2 instance offering descriptions takes this form ...

    http://<host>/awsec2offering/api/<availabilityZone>/<productDescription>/<offeringType>/<instanceType>{,<instanceType>}
    
- *&lt;host&gt;* is the server.  A permanent host has not yet been selected.  It is temporarily hosted at <code>p1software-eb1.elasticbeanstalk.com</code>.
- *&lt;availabilityZone&gt;* if omitted, defaults to <code>us-east-1</code>.
- *&lt;productDescription&gt;* if omitted, defaults to <code>linux</code>.  Recognized values are:
    - <code>linux</code>" (abbreviation of "Linux/UNIX")
    - <code>linuxvpc</code> (abbreviation of "Linux/UNIX VPC")
    - <code>windows</code> (abbreviation of "Windows")
    - <code>windowsvpc</code> (abbreviation of "Windows VPC")
- *&lt;offeringType&gt;* if omitted, is treated as a wildcard.  Recognized values are:
    - <code>light</code> (abbreviation of "Light Utilization")
    - <code>medium</code> (abbreviation of "Medium Utilization")
    - <code>heavy</code> (abbreviation of "Heavy Utilization")
- *&lt;instanceType&gt;* if omitted, is treated as a wildcard.  Multiple instance types may be specified using a comma separator.  Consult the AWS pricing [page](http://aws.amazon.com/ec2/pricing/) for the recognized instance types.

This sample URL requests instance descriptions in <code>us-east-1a</code> for <code>heavy</code> utilization <code>linux</code> servers in both <code>t1.micro</code> and <code>m1.small</code> instance types.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[http://p1software-eb1.elasticbeanstalk.com/awsec2offering/api/us-east-1a/linux/heavy/t1.micro,m1.small]( http://p1software-eb1.elasticbeanstalk.com/awsec2offering/api/us-east-1a/linux/heavy/t1.micro,m1.small)


Response
--------
The returned result is a JSON object whose ec2offering value is an array of EC2 descriptions (pretty printed here) like this ...

    {"ec2offerings":
    [{"availabilityZone":"us-east-1a","offeringType":"On-Demand","instanceType":"t1.micro","productDescription":"Linux/UNIX","duration":0,"currencyCode":"USD","fixedPrice":0.0,"hourlyPrice":0.02}
    ,{"availabilityZone":"us-east-1a","offeringType":"Heavy Utilization","instanceType":"t1.micro","productDescription":"Linux/UNIX","duration":94608000,"currencyCode":"USD","fixedPrice":100.0,"hourlyPrice":0.0050}
    ,{"availabilityZone":"us-east-1a","offeringType":"Heavy Utilization","instanceType":"t1.micro","productDescription":"Linux/UNIX","duration":31536000,"currencyCode":"USD","fixedPrice":62.0,"hourlyPrice":0.0050}
    ,{"availabilityZone":"us-east-1a","offeringType":"On-Demand","instanceType":"m1.small","productDescription":"Linux/UNIX","duration":0,"currencyCode":"USD","fixedPrice":0.0,"hourlyPrice":0.06}
    ,{"availabilityZone":"us-east-1a","offeringType":"Heavy Utilization","instanceType":"m1.small","productDescription":"Linux/UNIX","duration":94608000,"currencyCode":"USD","fixedPrice":257.0,"hourlyPrice":0.012}
    ,{"availabilityZone":"us-east-1a","offeringType":"Heavy Utilization","instanceType":"m1.small","productDescription":"Linux/UNIX","duration":31536000,"currencyCode":"USD","fixedPrice":169.0,"hourlyPrice":0.014}
    ]}


Price Comparison Chart
----------------------
Also see the companion [AWS EC2 Price Comparison Chart](https://github.com/kenklin/aws-price-comparison-chart) is an example of what a client might do with the awsec2offering web service.

[![Alt text](https://raw2.github.com/kenklin/aws-price-comparison-chart/master/aws-price-comparison-chart-small.png)]
(https://github.com/kenklin/aws-price-comparison-chart)

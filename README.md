AWS EC2 Offering Description Web Service (awsec2offering)
=========================================================
Software vendors using Amazon Web Service's Elastic Compute Cloud are faced with understanding different EC2 offerings and their [costs](http://aws.amazon.com/ec2/purchasing-options/reserved-instances/).

This RESTful web service returns both AWS EC2 on-demand and reserved instance offering descriptions which include both fixed and monthly costs.
- Reserved instance offering information is obtained from AWS via [describeReservedInstanceListing](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/AmazonEC2Client.html#describeReservedInstancesListings()) calls.  While information is cached on the awsec2offering server, it is purged every 24 hours.
- On-demand instance offering information is not availabe via the AWS SDK.  Therefore it has been hand transcribed from the AWS EC2 Pricing [page](http://aws.amazon.com/ec2/pricing/) into a JSON [file](https://github.com/kenklin/awsec2offering/blob/master/src/main/resources/aws-ec2-ondemand.json) which is read over the network by the awsec2offering server.  This information is also purged every 24 hours.  
 
If updates to the on-demand price file are needed, please contribute by either:
- Post it as an [issue](https://github.com/kenklin/awsec2offering/issues), or better yet,
- Modify  [aws-ec2-ondemand.json](https://github.com/kenklin/awsec2offering/blob/master/src/main/resources/aws-ec2-ondemand.json) and send a pull request.


Also see the companion [AWS EC2 Price Comparison Chart](https://github.com/kenklin/aws-price-comparison-chart).

[![Alt text](https://raw2.github.com/kenklin/aws-price-comparison-chart/master/aws-price-comparison-chart-small.png)]
(https://github.com/kenklin/aws-price-comparison-chart)

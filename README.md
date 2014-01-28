AWS EC2 Offering Description Web Service (awsec2offering)
==============
Software vendors using Amazon Web Service's Elastic Compute Cloud are faced with understanding different EC2 offerings and their [costs](http://aws.amazon.com/ec2/purchasing-options/reserved-instances/).

This RESTful web service returns both AWS EC2 on-demand and reserved instance offering descriptions which include both fixed and monthly costs.
- Reserved instance offering information is obtained from AWS via [describeReservedInstanceListing](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/AmazonEC2Client.html#describeReservedInstancesListings()) calls.  While information is cached on the awsec2offering server, it is purged every 24 hours.
- On-demand instance offering information is not availabe via the AWS SDK.  Therefore it has been hand transcribed from the AWS EC2 Pricing [page](http://aws.amazon.com/ec2/pricing/) into a JSON [file](https://github.com/kenklin/awsec2offering/blob/master/src/main/resources/aws-ec2-ondemand.json) which is read over the network by the awsec2offering server.  This information is also purged every 24 hours.  If updates are needed please post a request [here](https://github.com/kenklin/awsec2offering/blob/master/src/main/resources/aws-ec2-ondemand.json), or better yet, send a pull request.

See the companion [AWS EC2 Price Comparison Chart](https://github.com/kenklin/aws-price-comparison-chart) for an interactive chart.

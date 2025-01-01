## Steps to reproduce

1. Run the test `com.example.demo.DemoApplicationTests#startup`
2. After around 1 minute into the test execution, the following warning will be printed
> WARN 81434 --- [micrometer-demo] [r-kafka-metrics] i.m.p.PrometheusMeterRegistry            : The meter (MeterId{name='kafka.consumer.fetch.manager.bytes.consumed.rate', tags=[tag(client.id=consumer-first-group-id-1),tag(kafka.version=3.8.1),tag(spring.id=kafkaConsumerFactory.consumer-first-group-id-1)]}) registration has failed: Prometheus requires that all meters with the same name have the same set of tag keys. There is already an existing meter named 'kafka.consumer.fetch.manager.bytes.consumed.rate' containing tag keys [client.id, kafka.version, spring.id, topic]. The meter you are attempting to register has keys [client.id, kafka.version, spring.id]. Note that subsequent logs will be logged at debug level.
3. The expected result is that the warning should not be printed but it is inevitable (see Explanation section below)

## Explanation

This is a problem with the prometheus java client that is used internally by micrometer.
Prometheus lib doesn't allow registering the same metric name with different tag keys.
For example, the combination of these two metrics is illegal:
- request_count{name=one}
- request_count{name=one, error=someError}
  
In this case, prometheus lib would throw an exception when the second one is registered (or vice versa).
To avoid propagating the exception to the user code, micrometer simply ignores the second metric and never registers it.
It has always been like this, but this silent ignore led to users wasting time troubleshooting where their metrics were getting lost.
To help the troubleshooting process they now added this warning to give more information to the user.

Unfortunately in our case the issue originates from kafka.
The explanation for this is in this comment: https://github.com/micrometer-metrics/micrometer/issues/3508#issuecomment-1303700181
Basically, the absence/presence of topic tag is dependent on partition assignment by kafka broker, which is dynamic.

So there is no way to fix this unless prometheus lib would lift the arbitrary requirement. 
There is an open issue for them about this but they seem to not care: https://github.com/prometheus/client_java/issues/696

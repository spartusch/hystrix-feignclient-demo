Feign REST clients and Hystrix
==============================

This project demonstrates the behavior of Feign clients if Hystrix is enabled.

Why?
----

Well, the actual behavior of Feign clients with Hystrix enabled might be surprising. Feign clients throw an exception
whenever they receive a HTTP status code >= 300. And these exceptions can cause Hystrix to open circuits.

Thus you might run into trouble if you use Feign clients to communicate with a REST service that does make use of HTTP
status codes to indicate business results rather than purely technical issues.

So, depending on the service, HTTP status codes like `304 (Not Modified)`, `404 (Not Found)`, `409 (Conflict)` or
`412 (Precondition Failed)` might be expected and should not cause Hystrix to open a circuit.

Solution
--------

Hystrix doesn't open a circuit for exceptions derived from `com.netflix.hystrix.exception.HystrixBadRequestException`.
So a possible solution is to implement an `feign.codec.ErrorDecoder` and to map the status codes that shouldn't cause
Hystrix to open circuits to exceptions derived from `HystrixBadRequestException`.

How to run this project
-----------------------

This project demonstrates several different configurations of Feign clients, with and without an error decoder set.
This makes the issue more visible and allows you to play around a bit.

All the project's Feign clients live in `com.github.spartusch.hfdemo.clients`. There is a simple service in
`com.github.spartusch.hfdemo.service`. This service is used in a couple of test cases. Each test case shows some other
aspect of Feign with Hystrix.

You can find all the test cases in `com.github.spartusch.hfdemo.service.DemoServiceImplTest`.
Simply run those test cases using gradle or, preferably, your favorite IDE.

Links
-----

- https://github.com/Netflix/Hystrix/wiki/How-it-Works#CircuitBreaker
- http://projects.spring.io/spring-cloud/spring-cloud.html#spring-cloud-feign
- https://github.com/OpenFeign/feign

---
Stefan Partusch, 2017

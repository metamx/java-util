[![Build Status](https://travis-ci.org/metamx/java-util.svg?branch=master)](https://travis-ci.org/metamx/java-util)

# `com.metamx.common`

Utility code for java and jvm-based languages.

# `com.metamx.http` - HTTP client

This is an async HTTP client library that provides two basic functions:

 1) a channel caching pool for Netty channels that works well for inter-service communication
 2) the absolute minimum abstractions on top of Netty's HTTP support to make it a little easier to issue requests
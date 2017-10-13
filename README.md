[![Build Status](https://travis-ci.org/metamx/java-util.svg?branch=master)](https://travis-ci.org/metamx/java-util)

`com.metamx.common`
===================

Utility code for java and jvm-based languages.

`com.metamx.http` - HTTP client
===============================

This is an async HTTP client library that provides two basic functions:

 1) a channel caching pool for Netty channels that works well for inter-service communication
 2) the absolute minimum abstractions on top of Netty's HTTP support to make it a little easier to issue requests

`com.metamx.emitter` - Event Emitter
====================================

## Emitter creation
The easiest way to create an emmiter instance is via `com.metamx.emitter.core.Emitters.create` method.
This method will create one of the predefined emitters or your own implementation based on the properties provided.
`com.metamx.emitter.type` property value defines what Emitter should be created.

## Emitter types

### Logging emitter
`com.metamx.emitter.type = logging` will create `LoggingEmitter`.
For more details on `LoggingEmitter` configuration please refer `com.metamx.emitter.core.LoggingEmitterConfig`

### Http emitter
`com.metamx.emitter.type = http` will create `HttpPostEmitter`.
The only required parameter is `com.metamx.emitter.recipientBaseUrl` that is a url where all the events will be sent too.
For more details on `HttpPostEmitter` configuration please refer `com.metamx.emitter.core.HttpEmitterConfig`

### Parametrized URI http emitter
`com.metamx.emitter.type = parametrized` will create `ParametrizedUriEmitter`.
`ParametrizedUriEmitter` is a http emitter that can be used when events should be posted to different url based on event data.
The URI pattern is defined via `com.metamx.emitter.recipientBaseUrlPattern` property.
For instance: `com.metamx.emitter.recipientBaseUrlPattern=http://example.com/{feed}` will make it send events to different endpoints according to `event.getFeed` value.
`com.metamx.emitter.recipientBaseUrlPattern=http://example.com/{key1}/{key2}` requires that `key1` and `key2` are defined in event map.

### Custom emitter
To create your own emitter you need to implement EmitterFactory and add it as registered type to `ObjectMapper` that is used to call `Emitters.create`.
`com.metamx.emitter.type` property should be set to the type name of registered subclass that is usually set with an annotation `@JsonTypeName("my_custom_emitter")`
You can refer to `com.metamx.emitter.core.CustomEmitterFactoryTest` for an example of custom emitter creation.
All properties with `com.metamx.emitter.*` prefix will be translated into configuration json used to create `Emitter`.
Consider the following example:
```properties
com.metamx.emitter.type = my_custom_emitter
com.metamx.emitter.key1 = val1
com.metamx.emitter.outer.inner1 = inner_val1
com.metamx.emitter.outer.inner2 = inner_val2
```
will be translated into:
```json
{
  "type": "my_custom_emitter",
  "key1": "val1",
  "outer":
    {
      "inner1": "inner_val1",
      "inner2": "inner_val2"
    }
}
```
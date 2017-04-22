[![Build Status](https://travis-ci.org/lev-kuznetsov/jsonpath-jaxrs-filter.svg?branch=master)](https://travis-ci.org/lev-kuznetsov/jsonpath-jaxrs-filter) [![Coverage Status](https://coveralls.io/repos/github/lev-kuznetsov/jsonpath-jaxrs-filter/badge.svg?branch=master)](https://coveralls.io/github/lev-kuznetsov/jsonpath-jaxrs-filter?branch=master)

# jsonpath-jaxrs-filter

Applies a filter to responses using an expression found in the incoming request's `JSONPath` header

`JsonPathFilter` is annotated for autoscan discovery - simply add the jar to your dependencies to make use of this feature. Filter is enabled for `application/json`, `application/yaml`, `text/yaml`, `application/xml`, and `text/xml` entity media types

See [jayway](https://github.com/json-path/JsonPath) for the actual jsonpath implementation used

By default a custom Jackson based implementation of jayway's `JsonProvider` is used, one which can deal with beans directly. You may override the provider by declaring a `ContextResolver` supplying your implementation. You can configure `ObjectMapper` used by the provider implementation by doing the same - declare a provider supplying your mapper

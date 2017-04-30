[![Build Status](https://travis-ci.org/lev-kuznetsov/jsonpath-jaxrs-filter.svg?branch=master)](https://travis-ci.org/lev-kuznetsov/jsonpath-jaxrs-filter) [![Coverage Status](https://coveralls.io/repos/github/lev-kuznetsov/jsonpath-jaxrs-filter/badge.svg?branch=master)](https://coveralls.io/github/lev-kuznetsov/jsonpath-jaxrs-filter?branch=master) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/us.levk/jsonpath-jaxrs-filter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/us.levk/jsonpath-jaxrs-filter) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/3d9b578b70f940ae883b2815cd0912f2)](https://www.codacy.com/app/lev-kuznetsov/jsonpath-jaxrs-filter?utm_source=github.com&utm_medium=referral&utm_content=lev-kuznetsov/jsonpath-jaxrs-filter&utm_campaign=badger)

# jsonpath-jaxrs-filter

`JsonPathFilter` is annotated for autoscan discovery - simply add the jar to your dependencies to make use of this feature. Filter is enabled for all entity media types. As long as an incoming request specifies a jsonpath in `JSONPath` incoming header the filter will be applied

See [jayway](https://github.com/json-path/JsonPath) for the actual jsonpath implementation used

By default a custom Jackson based implementation of jayway's `JsonProvider` is used, one which can deal with beans directly. You may override the provider by declaring a `ContextResolver` supplying your implementation. You can configure `ObjectMapper` used by the provider implementation by doing the same - declare a provider supplying your mapper

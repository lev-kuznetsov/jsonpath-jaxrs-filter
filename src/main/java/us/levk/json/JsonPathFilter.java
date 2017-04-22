/*
 * The MIT License (MIT)
 * Copyright (c) 2017 lev.v.kuznetsov@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package us.levk.json;

import static com.jayway.jsonpath.Configuration.builder;
import static com.jayway.jsonpath.JsonPath.parse;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.of;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.json.JsonProvider;

/**
 * JAX-RS response jsonpath filter
 * 
 * @author levk
 */
@Provider
@Produces ({ APPLICATION_JSON, "application/yaml", "text/yaml" })
public class JsonPathFilter implements ContainerResponseFilter {

  /**
   * Providers
   */
  private @Context Providers providers;

  /**
   * @param q
   *          request
   * @param t
   *          type
   * @return instance
   */
  private <T> Optional <T> resolve (ContainerResponseContext r, Class <T> t) {
    return of (r.getMediaType (), null).map (m -> {
      return providers.getContextResolver (t, m);
    }).filter (c -> c != null).findFirst ().map (c -> c.getContext (t));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.
   * ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
   */
  @Override
  public void filter (ContainerRequestContext q, ContainerResponseContext r) throws IOException {
    ofNullable (q.getHeaders ().getFirst ("JSONPath")).filter (j -> !"$..*".equals (j)).ifPresent (j -> {
      r.setEntity (parse (r.getEntity (), builder ().jsonProvider (resolve (r, JsonProvider.class).orElseGet ( () -> {
        return new JacksonBeanJsonProvider2 (resolve (r, ObjectMapper.class).orElseGet (ObjectMapper::new));
      })).build ()).read (j));
    });
  }
}

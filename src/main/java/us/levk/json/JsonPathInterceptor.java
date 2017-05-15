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
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.io.IOException;
import java.util.Optional;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.spi.json.JsonProvider;

/**
 * JAX-RS response jsonpath filter
 * 
 * @author levk
 */
@Provider
@Produces (WILDCARD)
public class JsonPathInterceptor implements WriterInterceptor {

  /**
   * Providers
   */
  private @Context Providers providers;
  /**
   * Request headers
   */
  private @Context HttpHeaders headers;

  /**
   * @param q
   *          request
   * @param t
   *          type
   * @return instance
   */
  private <T> Optional <T> resolve (WriterInterceptorContext c, Class <T> t) {
    return of (c.getMediaType(), null).map (m -> providers.getContextResolver (t, m)).filter (r -> {
      return r != null;
    }).findFirst ().map (r -> r.getContext (t));
  }

  /*
   * (non-Javadoc)
   * @see javax.ws.rs.ext.WriterInterceptor#aroundWriteTo(javax.ws.rs.ext.WriterInterceptorContext)
   */
  @Override
  public void aroundWriteTo (WriterInterceptorContext c) throws IOException, WebApplicationException {
    ofNullable (headers.getHeaderString ("JSONPath")).filter (j -> !"$..*".equals (j)).ifPresent (j -> {
      c.setEntity (parse (c.getEntity (), builder ().jsonProvider (resolve (c, JsonProvider.class).orElseGet ( () -> {
        return new JacksonBeanJsonProvider (resolve (c, ObjectMapper.class).orElseGet (ObjectMapper::new));
      })).build ()).read ((String) j));
    });
    c.proceed ();
  }
}

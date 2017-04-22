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

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.resteasy.mock.MockDispatcherFactory.createDispatcher;
import static org.jboss.resteasy.mock.MockHttpRequest.get;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class JsonPathFilterTest {

  Dispatcher d;
  ObjectMapper m;

  @Before
  public void setup () {
    d = createDispatcher ();
    d.getProviderFactory ().register (JsonPathFilter.class);
    d.getProviderFactory ().register (JacksonJsonProvider.class);
    d.getRegistry ().addSingletonResource (new Bar ());
    m = new ObjectMapper ();
  }

  <T> T invoke (Class <T> c, MockHttpRequest q) throws IOException {
    MockHttpResponse r = new MockHttpResponse ();
    d.invoke (q, r);
    return m.readerFor (c).readValue (r.getOutput ());
  }

  @Test
  public void filter () throws Exception {
    assertThat (invoke (Set.class, get ("/bar").accept (APPLICATION_JSON).header ("JSONPath", "$.c[*].c[*].v")),
                is (new HashSet <> (asList ("c1c1", "c1c2", "c1c3", "c3c1"))));
  }
}

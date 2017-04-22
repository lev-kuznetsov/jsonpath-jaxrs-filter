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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonBeanJsonProviderTest {

  JacksonBeanJsonProvider p = new JacksonBeanJsonProvider (new ObjectMapper ());

  @Test
  public void getArrayIndex () {
    assertThat (p.getArrayIndex (asList ("foo", "bar"), 1), is ("bar"));
    assertThat (p.getArrayIndex (new int[] { 1, 2, 3 }, 1), is (2));
  }

  @Test
  public void getMapValue () {
    assertThat (p.getMapValue (new Object () {
      @JsonProperty String foo = "foo";
    }, "foo"), is ("foo"));
  }

  @Test
  public void getPropertyKeys () {
    assertThat (p.getPropertyKeys (new Object () {
      @JsonProperty String foo;

      @JsonProperty
      String bar () {
        throw new RuntimeException ();
      }
    }), allOf (hasItem ("foo"), hasItem ("bar")));
  }

  @Test
  public void isArray () {
    assertTrue (p.isArray (asList ("a", "b")));
    assertTrue (p.isArray (new boolean[] {}));
  }

  @Test
  public void isMap () {
    assertTrue (p.isMap (new Object ()));
    assertFalse (p.isMap (new long[] { 1L }));
  }

  @Test
  public void length () {
    assertThat (p.length (new Object () {
      @JsonProperty String foo;
      @JsonProperty String bar;
    }), is (2));
    assertThat (p.length (new int[] { 1, 2, 3 }), is (3));
  }

  @Test
  public void setArrayIndex () {
    int[] i = new int[] { 1, 2, 3 };
    p.setArrayIndex (i, 1, 4);
    assertThat (i[1], is (4));
  }

  @SuppressWarnings ("unchecked")
  @Test
  public void toIterable () {
    assertThat ((Iterable <Integer>) p.toIterable (new int[] { 1, 2, 3 }),
                allOf (hasItem (1), hasItem (2), hasItem (3)));
    assertThat ((Iterable <Integer>) p.toIterable (asList (1, 2, 3)), allOf (hasItem (1), hasItem (2), hasItem (3)));
  }

  @Test
  public void setProperty () {
    Map <String, String> m = new HashMap <> ();
    m.put ("foo", "bar");
    p.setProperty (m, "foo", "baz");
    assertThat (m.get ("foo"), is ("baz"));
  }

  @Test
  public void removeProperty () {
    Map <String, String> m = new HashMap <> ();
    m.put ("foo", "bar");
    p.removeProperty (m, "foo");
    assertFalse (m.containsKey ("foo"));
  }
}

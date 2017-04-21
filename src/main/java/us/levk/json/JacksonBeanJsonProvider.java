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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;

/**
 * JsonPath JSON provider for lazy value fetching from Jackson beans
 * 
 * @author levk
 */
public class JacksonBeanJsonProvider extends JacksonJsonProvider {

  /**
   * @param m
   *          object mapper
   */
  public JacksonBeanJsonProvider (ObjectMapper m) {
    super (m);
  }

  /**
   * @param c
   *          bean type
   * @return bean property writer stream if the type would be serialized as a
   *         bean, empty optional otherwise
   */
  private Optional <Stream <BeanPropertyWriter>> getBeanProperties (Class <?> c) {
    try {
      ObjectMapper m = getObjectMapper ();
      JsonSerializer <?> j =
          m.getSerializerFactory ().createSerializer (m.getSerializerProviderInstance (), m.constructType (c));
      return j instanceof BeanSerializer ? of ((BeanSerializer) j).map (b -> {
        return stream (spliteratorUnknownSize (b.properties (), IMMUTABLE), true).map (p -> (BeanPropertyWriter) p);
      }) : empty ();
    } catch (JsonMappingException e) {
      throw new JsonPathException (e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#isArray(java.lang.Object)
   */
  @Override
  public boolean isArray (Object o) {
    return o instanceof List || (o != null && o.getClass ().isArray ());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#isMap(java.lang.Object)
   */
  @Override
  public boolean isMap (Object o) {
    if (o == null) return false;
    Class <?> c = o.getClass ();
    return !c.isPrimitive () && c != String.class && c != Boolean.class && !Number.class.isAssignableFrom (c)
           && !isArray (o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#getArrayIndex(java.lang.
   * Object, int)
   */
  @Override
  public Object getArrayIndex (Object o, int i) {
    return o instanceof List ? ((List <?>) o).get (i) : Array.get (o, i);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#getMapValue(java.lang.
   * Object, java.lang.String)
   */
  @Override
  public Object getMapValue (Object o, String k) {
    if (o instanceof Map) return super.getMapValue (o, k);
    return getBeanProperties (o.getClass ()).map (b -> b.filter (p -> p.getName ().equals (k)).findAny ().map (p -> {
      try {
        return p.get (o);
      } catch (Exception e) {
        throw new JsonPathException (e);
      }
    }).orElse (UNDEFINED)).orElseGet ( () -> getMapValue (getObjectMapper ().convertValue (o, Map.class), k));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#getPropertyKeys(java.lang
   * .Object)
   */
  @Override
  public Collection <String> getPropertyKeys (Object o) {
    if (o instanceof Map) return super.getPropertyKeys (o);
    return getBeanProperties (o.getClass ()).map (b -> {
      return (Collection <String>) b.map (p -> p.getName ()).collect (toList ());
    }).orElseGet ( () -> getPropertyKeys (getObjectMapper ().convertValue (o, Map.class)));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#length(java.lang.Object)
   */
  @Override
  public int length (Object o) {
    return isArray (o) ? (o instanceof List ? ((List <?>) o).size () : Array.getLength (o))
                       : getPropertyKeys (o).size ();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#setArrayIndex(java.lang.
   * Object, int, java.lang.Object)
   */
  @SuppressWarnings ("unchecked")
  @Override
  public void setArrayIndex (Object o, int i, Object v) {
    if (o instanceof List) ((List <Object>) o).set (i, v);
    else Array.set (o, i, v);
  }
}

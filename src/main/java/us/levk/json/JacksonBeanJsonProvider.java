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

import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.lang.reflect.Array.set;
import static java.util.Optional.ofNullable;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static java.util.stream.StreamSupport.stream;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;

/**
 * Jackson based JSON provider for jayway JSONPath implementation, this provider
 * will lazily fetch properties from a bean object
 * 
 * @author levk
 */
public class JacksonBeanJsonProvider extends JacksonJsonProvider {

  /**
   * @param m
   *          object mapper to use
   */
  public JacksonBeanJsonProvider (ObjectMapper m) {
    super (m);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#isMap(java.lang.Object)
   */
  @Override
  public boolean isMap (Object o) {
    if (o == null || isArray (o)) return false;
    Class <?> c = o.getClass ();
    return !(c.isPrimitive () || Number.class.isAssignableFrom (c) || Boolean.class == c || String.class == c);
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
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#getArrayIndex(java.lang.
   * Object, int)
   */
  @Override
  public Object getArrayIndex (Object o, int i) {
    return o instanceof List ? super.getArrayIndex (o, i) : get (o, i);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#setArrayIndex(java.lang.
   * Object, int, java.lang.Object)
   */
  @Override
  public void setArrayIndex (Object o, int i, Object v) {
    if (o instanceof List) super.setArrayIndex (o, i, v);
    else set (o, i, v);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#setProperty(java.lang.
   * Object, java.lang.Object, java.lang.Object)
   */
  @Override
  public void setProperty (Object o, Object k, Object v) {
    if (o instanceof Map) super.setProperty (o, k, v);
    else throw new JsonPathException ("Unable to set property " + k + " on " + o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#removeProperty(java.lang.
   * Object, java.lang.Object)
   */
  @Override
  public void removeProperty (Object o, Object k) {
    if (o instanceof Map) super.removeProperty (o, k);
    else throw new JsonPathException ("Unable to remove property " + k + " on " + o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#length(java.lang.Object)
   */
  @Override
  public int length (Object o) {
    return (o instanceof List || o instanceof Map) ? super.length (o)
                                                   : (isArray (o) ? getLength (o) : propertySuppliers (o).size ());
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
    return o instanceof Map ? super.getPropertyKeys (o) : propertySuppliers (o).keySet ();
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
    return o instanceof Map ? super.getMapValue (o, k)
                            : ofNullable (propertySuppliers (o).get (k)).map (s -> s.get ()).orElse (UNDEFINED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.jayway.jsonpath.spi.json.AbstractJsonProvider#toIterable(java.lang.
   * Object)
   */
  @Override
  public Iterable <? extends Object> toIterable (Object o) {
    return o instanceof Iterable ? super.toIterable (o)
                                 : range (0, length (o)).mapToObj (i -> get (o, i)).collect (toList ());
  }

  /**
   * @param o
   *          object
   * @return map of property name to property value supplier
   */
  private Map <String, Supplier <Object>> propertySuppliers (Object o) {
    try {
      ObjectMapper m = getObjectMapper ();
      JsonSerializer <?> j = m.getSerializerFactory ().createSerializer (m.getSerializerProviderInstance (),
                                                                         m.constructType (o.getClass ()));
      if (j instanceof BeanSerializer) {
        Stream <BeanProperty> s = stream (spliteratorUnknownSize (((BeanSerializer) j).properties (), IMMUTABLE), true);
        return s.collect (toMap (p -> p.getName (), p -> () -> {
          try {
            return ((BeanPropertyWriter) p).get (o);
          } catch (Exception e) {
            throw new InvalidJsonException (e);
          }
        }));
      } else {
        @SuppressWarnings ("unchecked") Map <String, Object> v = m.convertValue (o, Map.class);
        return v.keySet ().stream ().collect (toMap (k -> k, k -> () -> v.get (k)));
      }
    } catch (JsonMappingException e) {
      throw new InvalidJsonException (e);
    }
  }
}

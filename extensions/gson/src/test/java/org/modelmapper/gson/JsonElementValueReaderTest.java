package org.modelmapper.gson;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.testng.annotations.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Test
public class JsonElementValueReaderTest {
  private final JsonElementValueReader valueReader = new JsonElementValueReader();

  public static class Order {
    public int id;
    public Customer customer;
  }

  public static class Customer {
    public int id;
    public Address address;
  }

  public static class Address {
    public String street;
    public String city;
  }

  public void shouldMapFromJsonElement() throws Exception {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setFieldMatchingEnabled(true)
        .setSourceNameTokenizer(NameTokenizers.UNDERSCORE)
        .addValueReader(valueReader);

    String orderJson = "{\"id\":456, \"customer\":{\"id\":789, \"street_address\":\"123 Main Street\", \"address_city\":\"SF\"}}";
    JsonParser jsonParser = new JsonParser();
    JsonElement element = jsonParser.parse(orderJson);

    Order order = modelMapper.map(element, Order.class);

    assertEquals(order.id, 456);
    assertEquals(order.customer.id, 789);
    assertEquals(order.customer.address.street, "123 Main Street");
    assertEquals(order.customer.address.city, "SF");

    String flatOrderJson = "{\"id\":222, \"customer_id\":333, \"customer_street_address\":\"444 Main Street\", \"customer_address_city\":\"LA\"}";
    element = jsonParser.parse(flatOrderJson);

    order = modelMapper.map(element, Order.class, "flat");

    assertEquals(order.id, 222);
    assertEquals(order.customer.id, 333);
    assertEquals(order.customer.address.street, "444 Main Street");
    assertEquals(order.customer.address.city, "LA");
  }

  public void shouldGetElements() {
    String json = "{\"object\":{\"subkey\":\"subvalue\"}, \"array\":[\"elem1\", \"elem2\"], \"boolean\":true, \"number\":55, \"string\":\"foo\", \"null\": null}";
    JsonElement element = new JsonParser().parse(json);

    JsonObject objElem = (JsonObject) valueReader.get(element, "object");
    assertEquals(objElem.get("subkey").getAsString(), "subvalue");

    JsonArray arrayElem = (JsonArray) valueReader.get(element, "array");
    assertEquals(Arrays.asList(arrayElem.get(0).getAsString(), arrayElem.get(1).getAsString()),
        Arrays.asList("elem1", "elem2"));

    assertEquals(valueReader.get(element, "boolean"), true);
    assertEquals(((Number) valueReader.get(element, "number")).intValue(), 55);
    assertEquals(valueReader.get(element, "string"), "foo");
    assertNull(valueReader.get(element, "null"));
  }
}

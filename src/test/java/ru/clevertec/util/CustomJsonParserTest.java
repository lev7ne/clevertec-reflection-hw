package ru.clevertec.util;

import org.junit.jupiter.api.Test;
import ru.clevertec.model.Customer;
import ru.clevertec.model.Order;
import ru.clevertec.model.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class CustomJsonParserTest {

    @Test
    void testParseProduct() throws Exception {
        String jsonProduct = "{\n" +
                "  \"id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",\n" +
                "  \"name\": \"Lightsaber\",\n" +
                "  \"price\": 1299.99,\n" +
                "  \"priceList\": {\n" +
                "    \"e76b3726-73d7-43f3-a92d-2f72b9a5b6d1\": 1300.50,\n" +
                "    \"9d9b73d0-1e6f-455b-8b5e-6b73a2923a53\": 1285.75\n" +
                "  }\n" +
                "}";

        Product product = CustomJsonParser.toObjectFromJson(jsonProduct, Product.class);
        assertNotNull(product);
        assertEquals("Lightsaber", product.getName());
        assertEquals(1299.99, product.getPrice());
    }

    @Test
    void testParseOrder() throws Exception {
        String jsonOrder = "{\n" +
                "  \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                "  \"products\": [\n" +
                "    {\n" +
                "      \"id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",\n" +
                "      \"name\": \"Lightsaber\",\n" +
                "      \"price\": 1299.99,\n" +
                "      \"priceList\": {\n" +
                "        \"e76b3726-73d7-43f3-a92d-2f72b9a5b6d1\": 1300.50,\n" +
                "        \"9d9b73d0-1e6f-455b-8b5e-6b73a2923a53\": 1285.75\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"createDate\": \"2024-09-18T12:45:30Z\"\n" +
                "}";

        Order order = CustomJsonParser.toObjectFromJson(jsonOrder, Order.class);
        assertNotNull(order);
        assertEquals(1, order.getProducts().size());
    }

    @Test
    void testParseCustomer() throws Exception {
        String jsonCustomer = "{\n" +
                "  \"id\": \"e6af973b-0b68-4b39-9f4e-4f42a6f2e2f9\",\n" +
                "  \"firstName\": \"Luke\",\n" +
                "  \"lastName\": \"Skywalker\",\n" +
                "  \"dateBirth\": \"1985-12-19\",\n" +
                "  \"orders\": [\n" +
                "    {\n" +
                "      \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                "      \"products\": [\n" +
                "        {\n" +
                "          \"id\": \"f47ac10b-58cc-4372-a567-0e02b2c3d479\",\n" +
                "          \"name\": \"Lightsaber\",\n" +
                "          \"price\": 1299.99,\n" +
                "          \"priceList\": {\n" +
                "            \"e76b3726-73d7-43f3-a92d-2f72b9a5b6d1\": 1300.50,\n" +
                "            \"9d9b73d0-1e6f-455b-8b5e-6b73a2923a53\": 1285.75\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"createDate\": \"2024-09-18T12:45:30Z\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Customer customer = CustomJsonParser.toObjectFromJson(jsonCustomer, Customer.class);
        assertNotNull(customer);
        assertEquals("Luke", customer.getFirstName());
    }

}
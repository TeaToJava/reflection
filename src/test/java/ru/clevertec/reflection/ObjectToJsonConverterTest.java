package ru.clevertec.reflection;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import ru.clevertec.reflection.model.Order;
import ru.clevertec.reflection.model.Product;
import ru.clevertec.reflection.utils.ObjectToJsonConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObjectToJsonConverterTest {

    @Test
    void shouldCreateValidJson() throws IllegalAccessException {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCreateDate(OffsetDateTime
                .of(LocalDate.of(2022, 03, 04).atStartOfDay(),
                        ZoneId.of("Europe/Budapest").getRules().getOffset(LocalDateTime.now())));
        Product product = new Product();
        product.setName("U");
        product.setQuantityInStocks(Map.of(UUID.randomUUID(), BigDecimal.valueOf(1214124142)));
        Product product2 = new Product();
        product2.setName("U2");
        order.setProducts(List.of(product, product2));
        String actual = ObjectToJsonConverter.formatJson(ObjectToJsonConverter.createJson(order));
        String expected = new JSONObject(order).toString();
        assertEquals(expected, actual);
    }
}
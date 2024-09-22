package ru.clevertec.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;


@Data
public class Product {
    private UUID id;
    private String name;
    private Double price;
    private Map<UUID, BigDecimal> priceList;
}

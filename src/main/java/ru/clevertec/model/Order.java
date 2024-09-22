package ru.clevertec.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Data
public class Order {
    private UUID id;
    private List<Product> products;
    private OffsetDateTime createDate;
}

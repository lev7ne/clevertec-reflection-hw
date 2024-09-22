package ru.clevertec.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Data
public class Customer {
    private UUID id;
    private String firstName;
    private String lastName;
    private LocalDate dateBirth;
    private List<Order> orders;
}

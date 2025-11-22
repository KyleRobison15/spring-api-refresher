package com.codewithmosh.store.users;

import jakarta.persistence.*;
import lombok.*;

/**
 * Address entity - changed from @Entity to @Embeddable to match BaseUser pattern.
 * Now embedded in User via @ElementCollection instead of separate table.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Embeddable
public class Address {

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "zip_code")
    private String zipCode;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "is_default")
    private boolean isDefault;
}
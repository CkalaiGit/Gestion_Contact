package com.cairedine.gestion.contact.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Contact {
    //TODO: -> @Entity pour persister
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;


}

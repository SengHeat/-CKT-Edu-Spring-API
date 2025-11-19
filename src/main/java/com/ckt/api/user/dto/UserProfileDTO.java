package com.ckt.api.user.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatar;
    private Date dateOfBirth;
    private String status;
}

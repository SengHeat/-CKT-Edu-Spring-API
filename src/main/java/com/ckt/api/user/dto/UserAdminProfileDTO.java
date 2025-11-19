package com.ckt.api.user.dto;

import java.util.Date;
import java.util.List;

import com.ckt.api.user.model.entity.Permission;
import com.ckt.api.user.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminProfileDTO {
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
    private List<Role> roles;
}

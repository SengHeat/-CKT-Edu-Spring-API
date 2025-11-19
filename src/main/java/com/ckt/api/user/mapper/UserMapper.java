package com.ckt.api.user.mapper;

import com.ckt.api.user.dto.UserAdminProfileDTO;
import com.ckt.api.user.dto.UserProfileDTO;
import com.ckt.api.user.model.entity.User;

public class UserMapper {

    public static UserProfileDTO toDTO(User user) {
        if(user == null) {
            return null;
        }
        return UserProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .dateOfBirth(user.getDateOfBirth())
                .status(user.getStatus())
                .build();
    }

    public static UserAdminProfileDTO toAdminDTO(User user) {
        if(user == null) {
            return null;
        }
        return UserAdminProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .gender(user.getGender())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .dateOfBirth(user.getDateOfBirth())
                .status(user.getStatus())
                .roles(user.getRoles().stream().toList())
                .build();
    }
}

package com.nipuna.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
}


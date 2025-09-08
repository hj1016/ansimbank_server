package com.grandma.ansimbank.auth.dto;

import com.grandma.ansimbank.common.constants.ConnectionStatus;
import com.grandma.ansimbank.common.constants.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private UserType userType;
    private Long userId;
    private ConnectionStatus connectionStatus;

    public JwtResponse(String token, String username, UserType userType, Long userId, ConnectionStatus connectionStatus) {
        this.token = token;
        this.username = username;
        this.userType = userType;
        this.userId = userId;
        this.connectionStatus = connectionStatus;
    }
}
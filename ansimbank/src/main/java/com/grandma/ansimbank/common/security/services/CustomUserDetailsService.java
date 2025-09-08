package com.grandma.ansimbank.common.security.services;

import com.grandma.ansimbank.common.constants.UserType;
import com.grandma.ansimbank.user.entity.User;
import com.grandma.ansimbank.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        return UserPrincipal.create(user);
    }
    
    @Transactional
    public UserDetails loadUserByUsernameAndType(String username, UserType userType) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndUserType(username, userType)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        
        return UserPrincipal.create(user);
    }
}
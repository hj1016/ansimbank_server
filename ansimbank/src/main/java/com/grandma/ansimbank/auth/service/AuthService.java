package com.grandma.ansimbank.auth.service;

import com.grandma.ansimbank.auth.dto.*;
import com.grandma.ansimbank.common.constants.ConnectionStatus;
// UserType은 User.UserType으로 사용
import com.grandma.ansimbank.common.security.jwt.JwtUtil;
import com.grandma.ansimbank.common.security.services.CustomUserDetailsService;
import com.grandma.ansimbank.common.security.services.UserPrincipal;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.repository.FamilyConnectionRepository;
import com.grandma.ansimbank.user.UserRepository;
import com.grandma.ansimbank.family.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FamilyConnectionRepository familyConnectionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private FamilyService familyService;

    public ResponseEntity<?> registerUser(SignUpRequest signUpRequest) {
        // 사용자명 중복 체크
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("이미 사용 중인 사용자명입니다!"));
        }

        // 새 사용자 생성
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setName(signUpRequest.getName());
        user.setPhoneNumber(signUpRequest.getPhoneNumber());
        user.setUserType(signUpRequest.getUserType());
        // 임시 이메일 설정 (username + @temp.ansimbank.com)
        user.setEmail(signUpRequest.getUsername() + "@temp.ansimbank.com");

        // 가족 연동 요청이 있는 경우
        if (signUpRequest.getFamilyUsername() != null && !signUpRequest.getFamilyUsername().isEmpty()) {
            user.setRequestedFamilyId(signUpRequest.getFamilyUsername());
            user.setConnectionStatus(ConnectionStatus.PENDING);

            // 요청받을 사용자가 존재하는지 확인
            Optional<User> targetUser = userRepository.findByUsername(signUpRequest.getFamilyUsername());
            if (targetUser.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("연동 요청할 사용자를 찾을 수 없습니다."));
            }

            // 부모-자녀 관계 검증
            User target = targetUser.get();
            if ((signUpRequest.getUserType() == User.UserType.CHILD && target.getUserType() != User.UserType.PARENT) ||
                    (signUpRequest.getUserType() == User.UserType.PARENT && target.getUserType() != User.UserType.CHILD)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("잘못된 가족 관계입니다. 부모는 자녀에게, 자녀는 부모에게만 연동 요청할 수 있습니다."));
            }
        } else {
            user.setConnectionStatus(ConnectionStatus.APPROVED); // 단독 가입은 바로 승인
        }

        user = userRepository.save(user);
        
        // 가족 초대 처리 (가입한 전화번호로 온 초대들을 자동으로 연결)
        try {
            familyService.processInvitationsOnSignUp(user);
        } catch (Exception e) {
            // 초대 처리 실패해도 회원가입은 성공으로 처리
            System.err.println("가족 초대 처리 실패: " + e.getMessage());
        }

        return ResponseEntity.ok(new MessageResponse("회원가입이 완료되었습니다!"));
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        try {
            // 먼저 사용자가 존재하는지 확인
            User user = userRepository.findByUsernameAndUserType(
                            loginRequest.getUsername(), loginRequest.getUserType())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 비밀번호 검증
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("비밀번호가 일치하지 않습니다."));
            }

            // 인증 성공시 JWT 토큰 생성
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String jwt = jwtUtil.generateJwtToken(userPrincipal);

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userPrincipal.getUsername(),
                    userPrincipal.getUserType(),
                    userPrincipal.getId(),
                    user.getConnectionStatus()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("로그인에 실패했습니다: " + e.getMessage()));
        }
    }
}
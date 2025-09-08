package com.grandma.ansimbank.demo;

import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import com.grandma.ansimbank.account.Account;
import com.grandma.ansimbank.account.AccountRepository;
import com.grandma.ansimbank.common.response.ApiCommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class TestDataController {
    
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    
    @PostMapping("/init-data")
    public ResponseEntity<ApiCommonResponse<String>> initTestData() {
        
        // 부모 사용자 생성
        User parent = User.builder()
                .email("parent@test.com")
                .password("password123")
                .name("김부모")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1980, 5, 15))
                .userType(User.UserType.PARENT)
                .socialProvider(User.SocialProvider.NONE)
                .build();
        parent = userRepository.save(parent);
        
        // 자녀 사용자 생성
        User child = User.builder()
                .email("child@test.com")
                .password("password123")
                .name("김자녀")
                .phone("010-9876-5432")
                .birthDate(LocalDate.of(2005, 8, 20))
                .userType(User.UserType.CHILD)
                .socialProvider(User.SocialProvider.NONE)
                .build();
        child = userRepository.save(child);
        
        // 부모 계좌 생성
        Account parentAccount = Account.builder()
                .user(parent)
                .bankCode("001")
                .bankName("KB국민은행")
                .accountNumber("1234567890")
                .accountHolder("김부모")
                .isPrimary(true)
                .build();
        accountRepository.save(parentAccount);
        
        // 자녀 계좌 생성
        Account childAccount = Account.builder()
                .user(child)
                .bankCode("004")
                .bankName("신한은행")
                .accountNumber("9876543210")
                .accountHolder("김자녀")
                .isPrimary(true)
                .build();
        accountRepository.save(childAccount);
        
        String result = String.format(
                "테스트 데이터 생성 완료!\n" +
                "부모: ID=%d, 이름=%s, 계좌=%s\n" +
                "자녀: ID=%d, 이름=%s, 계좌=%s",
                parent.getUserId(), parent.getName(), parentAccount.getAccountNumber(),
                child.getUserId(), child.getName(), childAccount.getAccountNumber()
        );
        
        return ResponseEntity.ok(ApiCommonResponse.success(result));
    }
}
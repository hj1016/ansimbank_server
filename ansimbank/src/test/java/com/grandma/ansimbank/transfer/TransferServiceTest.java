package com.grandma.ansimbank.transfer;

import com.grandma.ansimbank.account.Account;
import com.grandma.ansimbank.account.AccountRepository;
import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.exception.CustomException;
import com.grandma.ansimbank.transaction.Transaction;
import com.grandma.ansimbank.transaction.TransactionRepository;
import com.grandma.ansimbank.transfer.dto.TransferRequestDTO;
import com.grandma.ansimbank.transfer.dto.TransferResponseDTO;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionAuditService transactionAuditService;

    private TransferService transferService;
    private User sender;
    private Account senderAccount;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(
                transactionRepository, userRepository, accountRepository, transactionAuditService);

        sender = User.builder().userId(1L).build();
        senderAccount = Account.builder()
                .user(sender)
                .accountNumber("1234567890")
                .balance(10_000L)
                .isActive(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountNumberAndUser_UserIdForUpdate("1234567890", 1L))
                .thenReturn(Optional.of(senderAccount));
    }

    @Test
    void transferCompletesAfterLockedBalanceDeduction() {
        TransferRequestDTO request = requestOf("1000");
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponseDTO response = transferService.transfer(request);

        assertThat(senderAccount.getBalance()).isEqualTo(9_000L);
        assertThat(response.getTransactionStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
        verify(accountRepository).findByAccountNumberAndUser_UserIdForUpdate("1234567890", 1L);
        verify(accountRepository).save(senderAccount);
    }

    @Test
    void transferRejectsFractionalWonWithoutSaving() {
        TransferRequestDTO request = requestOf("1000.5");

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(CustomException.class)
                .extracting(exception -> ((CustomException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_TRANSFER_AMOUNT);

        verify(transactionRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }

    private TransferRequestDTO requestOf(String amount) {
        return TransferRequestDTO.builder()
                .senderId(1L)
                .senderAccount("1234567890")
                .receiverAccount("0987654321")
                .receiverName("수취인")
                .receiverBank("테스트은행")
                .amount(new BigDecimal(amount))
                .build();
    }
}

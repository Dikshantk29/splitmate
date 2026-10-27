package com.splitmate.service;

import com.splitmate.domain.Expense;
import com.splitmate.domain.ExpenseShare;
import com.splitmate.domain.Household;
import com.splitmate.domain.Membership;
import com.splitmate.domain.Settlement;
import com.splitmate.domain.User;
import com.splitmate.repo.ExpenseRepository;
import com.splitmate.repo.ExpenseShareRepository;
import com.splitmate.repo.HouseholdRepository;
import com.splitmate.repo.MembershipRepository;
import com.splitmate.repo.SettlementRepository;
import com.splitmate.repo.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SettlementBusinessRulesTest {
    private UserRepository users;
    private HouseholdRepository households;
    private MembershipRepository memberships;
    private ExpenseRepository expenses;
    private ExpenseShareRepository shares;
    private SettlementRepository settlements;
    private SplitMateService service;
    private User payer;

    @BeforeEach
    void setUp() {
        users = org.mockito.Mockito.mock(UserRepository.class);
        households = org.mockito.Mockito.mock(HouseholdRepository.class);
        memberships = org.mockito.Mockito.mock(MembershipRepository.class);
        expenses = org.mockito.Mockito.mock(ExpenseRepository.class);
        shares = org.mockito.Mockito.mock(ExpenseShareRepository.class);
        settlements = org.mockito.Mockito.mock(SettlementRepository.class);
        PasswordEncoder encoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        service = new SplitMateService(users, households, memberships, expenses, shares, settlements, encoder);
        payer = new User("Roommate B", "b@example.com", "hash");
        payer.id = 2L;
    }

    @Test
    void rejectsPaymentLargerThanRemainingBalanceAfterPendingPayments() {
        prepareLedger(List.of(pending(2L, 1L, "40.00")));

        ApiException error = assertThrows(ApiException.class,
            () -> service.createSettlement(10L, 1L, new BigDecimal("10.01"), payer));

        assertEquals("Payment exceeds the remaining amount owed to this roommate (10.00)", error.getMessage());
    }

    @Test
    void permitsPaymentUpToRemainingBalance() {
        prepareLedger(List.of(pending(2L, 1L, "40.00")));
        when(settlements.save(any(Settlement.class))).thenAnswer(call -> call.getArgument(0));

        var created = service.createSettlement(10L, 1L, new BigDecimal("10.00"), payer);

        assertEquals(2L, created.payerId());
        assertEquals(1L, created.payeeId());
        assertEquals(new BigDecimal("10.00"), created.amount());
        assertEquals("PENDING", created.status());
    }

    private void prepareLedger(List<Settlement> priorSettlements) {
        Household home = new Household("Home", 1L);
        home.id = 10L;
        when(households.findByIdForUpdate(10L)).thenReturn(Optional.of(home));
        when(memberships.existsByHouseholdIdAndUserId(10L, 2L)).thenReturn(true);
        when(memberships.existsByHouseholdIdAndUserId(10L, 1L)).thenReturn(true);
        when(memberships.findByHouseholdId(10L)).thenReturn(List.of(
            new Membership(10L, 1L, "OWNER"), new Membership(10L, 2L, "MEMBER")));
        Expense expense = new Expense(10L, 1L, "Rent", "RENT", new BigDecimal("100.00"),
            LocalDate.of(2026, 1, 1), "EQUAL");
        expense.id = 99L;
        when(expenses.findByHouseholdId(10L)).thenReturn(List.of(expense));
        when(shares.findByExpenseId(99L)).thenReturn(List.of(
            new ExpenseShare(99L, 1L, new BigDecimal("50.00")),
            new ExpenseShare(99L, 2L, new BigDecimal("50.00"))));
        when(settlements.findByHouseholdIdOrderByCreatedAtDesc(10L)).thenReturn(priorSettlements);
        User payee = new User("Roommate A", "a@example.com", "hash");
        payee.id = 1L;
        when(users.findById(1L)).thenReturn(Optional.of(payee));
        when(users.findById(2L)).thenReturn(Optional.of(payer));
    }

    private Settlement pending(Long from, Long to, String amount) {
        Settlement settlement = new Settlement(10L, from, to, new BigDecimal(amount));
        settlement.status = "PENDING";
        return settlement;
    }
}

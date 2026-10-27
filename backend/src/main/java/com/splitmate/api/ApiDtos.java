package com.splitmate.api;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.List;
public final class ApiDtos { private ApiDtos(){}
 public record Register(@NotBlank @Size(max=100) String name,@NotBlank @Email String email,@NotBlank @Size(min=8,max=72) String password){}
 public record Login(@NotBlank @Email String email,@NotBlank String password){}
 public record AuthResponse(String token,Long userId,String name,String email){}
 public record CreateHousehold(@NotBlank @Size(max=120) String name){}
 public record AddMember(@NotBlank @Email String email){}
 public record Member(Long userId,String name,String email,String role){}
 public record HouseholdView(Long id,String name,List<Member> members){}
 public record ShareInput(@NotNull Long userId,@NotNull @DecimalMin("0.00") BigDecimal amount){}
 public record CreateExpense(@NotNull Long householdId,@NotBlank @Size(max=180) String description,@NotBlank String category,@NotNull @DecimalMin(value="0.01") BigDecimal amount,@NotNull LocalDate expenseDate,@NotBlank String splitType,List<ShareInput> shares){}
 public record ExpenseView(Long id,Long householdId,Long paidBy,String payerName,String description,String category,BigDecimal amount,LocalDate expenseDate,String splitType,List<ShareInput> shares){}
 public record Balance(Long userId,String name,BigDecimal net){}
 public record Transfer(Long payerId,String payerName,Long payeeId,String payeeName,BigDecimal amount){}
 public record SettlementView(Long id,Long payerId,String payerName,Long payeeId,String payeeName,BigDecimal amount,String status,String paidAt){}
 public record CreateSettlement(@NotNull Long payeeId,@NotNull @DecimalMin(value="0.01") BigDecimal amount){}
 public record MarkPaid(@NotNull Long settlementId){}
 public record Summary(BigDecimal monthlySpend,BigDecimal totalSpend,int expenseCount,List<Balance> balances,List<Transfer> suggestedTransfers){}
 public record PageView<T>(List<T> content,int page,int size,long totalElements,int totalPages){}
}

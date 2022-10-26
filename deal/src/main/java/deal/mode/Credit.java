package deal.mode;

import deal.enums.Status;

import java.math.BigDecimal;
import java.util.List;

public class Credit {
    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;
    private List<PaymentScheduleElement> paymentSchedule;
    private Boolean isInsuranceEnable;
    private Boolean isSalaryClient;
    private Status creditStatus;
}
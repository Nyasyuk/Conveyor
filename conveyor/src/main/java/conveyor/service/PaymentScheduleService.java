package conveyor.service;

import conveyor.dto.PaymentScheduleElementDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PaymentScheduleService {
    public List<PaymentScheduleElementDTO> calculatePaymentScheduleElementList(final Integer term,
                                                                               final BigDecimal totalAmount,
                                                                               final BigDecimal rate,
                                                                               final BigDecimal monthlyPayment) {
        log.info("calculatedInterestPaymentList method start");
        BigDecimal remainingDebt = totalAmount;
        log.info("remainingDebt = " + totalAmount);
        final List<PaymentScheduleElementDTO> paymentScheduleElementDTOS = new ArrayList<>();
        for (int i = 1; i <= term; i++) {
            log.info("create PaymentScheduleElement start");
            final BigDecimal interestPayment = calculatedInterestPayment(rate, remainingDebt);
            final BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
            remainingDebt = remainingDebt.subtract(debtPayment.add(interestPayment));
            PaymentScheduleElementDTO paymentScheduleElementDTO = new PaymentScheduleElementDTO(i,
                    LocalDate.now().plusMonths(Long.parseLong(Integer.toString(i))), monthlyPayment, interestPayment,
                    debtPayment, remainingDebt);
            paymentScheduleElementDTOS.add(paymentScheduleElementDTO);
            log.info(paymentScheduleElementDTO.toString());
            log.info("create PaymentScheduleElement end");
        }
        log.info("return " + paymentScheduleElementDTOS);
        log.info("calculatedInterestPaymentList method end");
        return paymentScheduleElementDTOS;
    }

    private BigDecimal calculatedInterestPayment(final BigDecimal rate, final BigDecimal amount) {
        log.info("calculatedInterestPayment method start");
        BigDecimal interestPayment = rate.divide(new BigDecimal("100"), MathContext.DECIMAL128)
                .divide(new BigDecimal("12"), MathContext.DECIMAL128)
                .multiply(amount, MathContext.DECIMAL128)
                .setScale(2, RoundingMode.HALF_UP);
        log.info("interestPayment = " + interestPayment);
        log.info("calculatedInterestPayment method end");
        return interestPayment;
    }
}

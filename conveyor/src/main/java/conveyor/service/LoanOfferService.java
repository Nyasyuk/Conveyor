package conveyor.service;

import conveyor.dto.LoanApplicationRequestDTO;
import conveyor.dto.LoanOfferDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LoanOfferService {

    @Value("${base-rate:}")
    private BigDecimal rate;

    public List<LoanOfferDTO> getLoanOffersCredit(LoanApplicationRequestDTO loanApplicationRequestDTO) {
        log.info("getLoanOffersCredit method start");
        List<LoanOfferDTO> loanOffers = new ArrayList<>();
        log.info("create LoanOfferDTO with isInsuranceEnabled = true, isSalaryClient = true");
        loanOffers.add(formationLoanOffer(true, true, loanApplicationRequestDTO));
        log.info("create LoanOfferDTO with isInsuranceEnabled = true, isSalaryClient = false");
        loanOffers.add(formationLoanOffer(true, false, loanApplicationRequestDTO));
        log.info("create LoanOfferDTO with isInsuranceEnabled = true, isSalaryClient = false");
        loanOffers.add(formationLoanOffer(false, true, loanApplicationRequestDTO));
        log.info("create LoanOfferDTO with isInsuranceEnabled = false, isSalaryClient = false");
        loanOffers.add(formationLoanOffer(false, false, loanApplicationRequestDTO));
        log.info("loanOffers = " + loanOffers);
        log.info("sorted list by rate");
        List<LoanOfferDTO> sortedLoanOffers = loanOffers.stream()
                .sorted(Comparator.comparing(LoanOfferDTO::getRate).reversed())
                .collect(Collectors.toList());
        log.info("getLoanOffersCredit method return " + sortedLoanOffers);
        log.info("getLoanOffersCredit method end");
        return sortedLoanOffers;
    }

    public LoanOfferDTO formationLoanOffer(boolean isInsuranceEnabled,
                                           boolean isSalaryClient, LoanApplicationRequestDTO dto) {
        log.info("formationLoanOffer method start");
        final BigDecimal newRate = calculatedRareByInsuranceEnabledAndSalaryClient(isInsuranceEnabled, isSalaryClient, rate);

        log.info("totalAmount calculation");
        BigDecimal totalAmount = calculatedTotalAmount(dto.getAmount(), dto.getTerm(), newRate, isInsuranceEnabled);
        log.info("paymentInMouth calculation");
        final BigDecimal paymentInMouth = calculatedPaymentInMouth(dto.getTerm(), totalAmount);
        totalAmount = correctingTotalAmount(paymentInMouth, dto.getTerm());
        LoanOfferDTO loanOfferDTO = new LoanOfferDTO(null, totalAmount,
                dto.getAmount(), dto.getTerm(), paymentInMouth, newRate, isInsuranceEnabled, isSalaryClient);
        log.info("loanOfferDto = " + loanOfferDTO);

        log.info("formationLoanOffer method return " + loanOfferDTO);
        log.info("formationLoanOffer method end");
        return loanOfferDTO;
    }

    public BigDecimal calculatedRareByInsuranceEnabledAndSalaryClient(boolean isInsuranceEnabled,
                                                                      boolean isSalaryClient, BigDecimal rate) {
        log.info("calculatedRareByInsuranceEnabledAndSalaryClient method start");
        BigDecimal newRate;

        log.info("isInsuranceEnabled = " + isInsuranceEnabled + ", isSalaryClient = " + isSalaryClient);
        log.info("newRate calculation");
        if (isInsuranceEnabled && isSalaryClient) {
            newRate = rate.subtract(new BigDecimal("3"));
        } else if (!isInsuranceEnabled && isSalaryClient) {
            newRate = rate.subtract(new BigDecimal("1"));
        } else if (isInsuranceEnabled) {
            newRate = rate.subtract(new BigDecimal("2"));
        } else {
            newRate = rate.add(new BigDecimal("2"));
        }
        log.info("newRate = " + newRate);
        log.info("calculatedRareByInsuranceEnabledAndSalaryClient method end");
        return newRate;
    }

    public BigDecimal calculatedTotalAmount(final BigDecimal amount, final Integer term,
                                            final BigDecimal newRate, final Boolean isInsuranceEnabled) {
        log.info("calculatedTotalAmount method start");
        log.info("isInsuranceEnabled = " + isInsuranceEnabled);
        BigDecimal newAmount;

        if (isInsuranceEnabled) {
            newAmount = amount.add(new BigDecimal("100000"), MathContext.DECIMAL128);
        } else {
            newAmount = amount;
        }
        log.info("newAmount = " + newAmount);

        BigDecimal totalAmount = newRate.divide(new BigDecimal("100"), MathContext.DECIMAL128)
                .multiply(new BigDecimal(term).divide(new BigDecimal("12")))
                .multiply(newAmount)
                .add(newAmount);
        log.info("totalAmount = " + totalAmount);
        log.info("calculatedTotalAmount method end");
        return totalAmount;
    }

    public BigDecimal calculatedPaymentInMouth(final Integer term, final BigDecimal totalAmount) {
        log.info("calculatedPaymentInMouth method start");
        BigDecimal bigDecimal = new BigDecimal(term);
        BigDecimal paymentInMouth = totalAmount.divide(bigDecimal, MathContext.DECIMAL128)
                .setScale(0, RoundingMode.CEILING);
        log.info("paymentInMouth = " + paymentInMouth);
        log.info("calculatedPaymentInMouth method end");
        return paymentInMouth;
    }

    public BigDecimal correctingTotalAmount(final BigDecimal paymentInMouth, final Integer term) {
        log.info("correctingTotalAmount method start");
        BigDecimal totalAmount = paymentInMouth.multiply(BigDecimal.valueOf(term));
        log.info("totalAmount = " + totalAmount);
        log.info("correctingTotalAmount method end");
        return totalAmount;
    }
}
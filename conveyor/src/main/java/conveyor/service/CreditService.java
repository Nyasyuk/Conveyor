package conveyor.service;

import conveyor.dto.CreditDTO;
import conveyor.dto.PaymentScheduleElementDTO;
import conveyor.dto.ScoringDataDTO;
import conveyor.exception.RefusalOfLoanException;
import conveyor.helperclass.enums.EmploymentStatus;
import conveyor.helperclass.enums.Gender;
import conveyor.helperclass.enums.MaritalStatus;
import conveyor.helperclass.enums.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class CreditService {

    @Value("${base-rate:}")
    private BigDecimal rate;
    private final LoanOfferService loanOfferService;
    private final PaymentScheduleService paymentScheduleService;

    CreditService(LoanOfferService loanOfferService, PaymentScheduleService paymentScheduleService) {
        this.loanOfferService = loanOfferService;
        this.paymentScheduleService = paymentScheduleService;
    }

    public CreditDTO getCalculationCredit(ScoringDataDTO scoringDataDTO) throws RefusalOfLoanException {
        log.info("getCalculationCredit method start");
        log.info("newRate calculation");
        BigDecimal newRate = rateCalculation(scoringDataDTO, rate);
        log.info("totalAmount calculation");
        BigDecimal totalAmount = loanOfferService.calculatedTotalAmount(scoringDataDTO.getAmount(),
                scoringDataDTO.getTerm(), newRate, scoringDataDTO.getIsInsuranceEnabled());
        log.info("monthlyPayment calculation");
        final BigDecimal monthlyPayment = loanOfferService.calculatedPaymentInMouth(scoringDataDTO.getTerm(),
                totalAmount);
        totalAmount = loanOfferService.correctingTotalAmount(monthlyPayment, scoringDataDTO.getTerm());
        log.info("psk calculation");
        final BigDecimal psk = calculationPSK(scoringDataDTO.getAmount(), totalAmount, scoringDataDTO.getTerm());
        log.info("paymentSchedule calculation");
        final List<PaymentScheduleElementDTO> paymentSchedule = paymentScheduleService.calculatePaymentScheduleElementList(scoringDataDTO.getTerm(),
                totalAmount, newRate, monthlyPayment);

        CreditDTO creditDTO = new CreditDTO(scoringDataDTO.getAmount(), scoringDataDTO.getTerm(), monthlyPayment,
                newRate, psk, scoringDataDTO.getIsInsuranceEnabled(), scoringDataDTO.getIsSalaryClient(), paymentSchedule);
        log.info("return " + creditDTO);
        log.info("getCalculationCredit method end");
        return creditDTO;
    }

    private BigDecimal rateCalculation(ScoringDataDTO scoringDataDTO, BigDecimal rate) throws RefusalOfLoanException {
        log.info("rateCalculation method start");
        BigDecimal newRate;
        newRate = rateCalculationWithEmploymentStatus(scoringDataDTO.getEmployment().getEmploymentStatus(), rate);
        newRate = rateCalculationWithPosition(scoringDataDTO.getEmployment().getPosition(), newRate);
        newRate = rateCalculationWithMaritalStatus(scoringDataDTO.getMaritalStatus(), newRate);
        newRate = rateCalculationWithDependentAmount(scoringDataDTO.getDependentAmount(), newRate);
        newRate = rateCalculationWithGenderAndAge(scoringDataDTO.getGender(), scoringDataDTO.getBirthdate(), newRate);
        newRate = rateCalculationWithIsInsuranceEnabledAndIsSalaryClient(scoringDataDTO.getIsInsuranceEnabled(), scoringDataDTO.getIsSalaryClient(), newRate);
        rateCalculationWithExperience(scoringDataDTO.getEmployment().getWorkExperienceCurrent(), scoringDataDTO.getEmployment().getWorkExperienceTotal());
        rateCalculationWithMinAmount(scoringDataDTO.getAmount(), scoringDataDTO.getEmployment().getSalary());
        log.info("return " + newRate);
        log.info("rateCalculation method end");
        return newRate;
    }

    private BigDecimal rateCalculationWithEmploymentStatus(EmploymentStatus employmentStatus, BigDecimal newRate) throws RefusalOfLoanException {
        log.info("rateCalculationWithEmploymentStatus method start");
        switch (employmentStatus) {
            case SELF_EMPLOYED:
                newRate = newRate.add(new BigDecimal("1"));
                break;
            case BUSINESS_OWNER:
                newRate = newRate.add(new BigDecimal("3"));
                break;
            case EMPLOYED:
                break;
            case UNEMPLOYED:
                throw new RefusalOfLoanException("Loan Denied");
            default:
                throw new IllegalArgumentException("Value not found");
        }
        log.info("employmentStatus = " + employmentStatus);
        log.info("return " + newRate);
        log.info("rateCalculationWithEmploymentStatus method end");
        return newRate;
    }

    private BigDecimal rateCalculationWithPosition(Position position, BigDecimal newRate) {
        log.info("rateCalculationWithPosition method start");
        switch (position) {
            case MID_MANAGER:
                newRate = newRate.subtract(new BigDecimal("2"));
                break;
            case TOP_MANAGER:
                newRate = newRate.subtract(new BigDecimal("4"));
                break;
            case OWNER:
            case WORKER:
                break;
            default:
                throw new IllegalArgumentException("Value not found");
        }
        log.info("position = " + position);
        log.info("return " + newRate);
        log.info("rateCalculationWithPosition method end");
        return newRate;
    }

    private BigDecimal rateCalculationWithMaritalStatus(MaritalStatus maritalStatus, BigDecimal newRate) {
        log.info("rateCalculationWithMaritalStatus method start");
        switch (maritalStatus) {
            case MARRIED:
                newRate = newRate.subtract(new BigDecimal("3"));
                break;
            case DIVORCED:
                newRate = newRate.add(new BigDecimal("1"));
                break;
            case SINGLE:
            case WIDOW_WIDOWER:
                break;
            default:
                throw new IllegalArgumentException("Value not found");
        }
        log.info("maritalStatus = " + maritalStatus);
        log.info("return " + newRate);
        log.info("rateCalculationWithMaritalStatus method end");
        return newRate;
    }

    private BigDecimal rateCalculationWithDependentAmount(Integer dependentAmount, BigDecimal newRate) {
        log.info("rateCalculationWithDependentAmount method start");
        log.info("dependentAmount = " + dependentAmount);
        if (dependentAmount > 1) {
            log.info("dependentAmount > 1");
            newRate = newRate.add(new BigDecimal("1"));
        }
        log.info("return " + newRate);
        log.info("rateCalculationWithDependentAmount method end");
        return newRate;
    }

    private BigDecimal rateCalculationWithGenderAndAge(Gender gender, LocalDate birthdate, BigDecimal newRate) throws RefusalOfLoanException {
        log.info("rateCalculationWithGenderAndAge method start");
        log.info("gender = " + gender);
        log.info("birthdate = " + birthdate);
        LocalDate currentDate = LocalDate.now();
        log.info("currentDate = " + currentDate);
        int years = (int) ChronoUnit.YEARS.between(birthdate, currentDate);
        log.info("years = " + years);
        if (years < 20 || years > 60) {
            throw new RefusalOfLoanException("Loan Denied");
        } else {
            if ((gender == Gender.FEMALE && years >= 35 && years <= 60)
                    || (gender == Gender.MALE && years >= 30 && years <= 55)) {
                newRate = newRate.subtract(new BigDecimal("3"));
            } else if (gender == Gender.NON_BINARY) {
                newRate = newRate.add(new BigDecimal("3"));
            }
        }
        log.info("return " + newRate);
        log.info("rateCalculationWithGenderAndAge method end");
        return newRate;
    }

    private BigDecimal rateCalculationWithIsInsuranceEnabledAndIsSalaryClient(final Boolean isInsuranceEnabled,
                                                                              final Boolean isSalaryClient, BigDecimal newRate) {
        log.info("rateCalculationWithIsInsuranceEnabledAndIsSalaryClient method start");
        newRate = loanOfferService.calculatedRareByInsuranceEnabledAndSalaryClient(isInsuranceEnabled,
                isSalaryClient, newRate);
        log.info("return " + newRate);
        log.info("rateCalculationWithIsInsuranceEnabledAndIsSalaryClient method end");
        return newRate;
    }

    private void rateCalculationWithExperience(Integer workExperienceCurrent, Integer workExperienceTotal) throws RefusalOfLoanException {
        log.info("rateCalculationWithExperience method start");
        log.info("workExperienceCurrent = " + workExperienceCurrent);
        log.info("workExperienceTotal = " + workExperienceTotal);
        if (workExperienceCurrent < 3 || workExperienceTotal < 12) {
            throw new RefusalOfLoanException("Loan Denied");
        }
        log.info("rateCalculationWithExperience method end");
    }

    private void rateCalculationWithMinAmount(BigDecimal amount, BigDecimal salary) throws RefusalOfLoanException {
        log.info("rateCalculationWithMaxLoanAmount method start");
        if (amount.compareTo(salary.multiply(new BigDecimal(20))) > 0) {
            throw new RefusalOfLoanException("Loan Denied");
        }
        log.info("rateCalculationWithMaxLoanAmount method end");
    }

    private BigDecimal calculationPSK(BigDecimal amount, BigDecimal totalAmount, Integer term) {
        log.info("calculationPSK method start");
        BigDecimal psk = totalAmount.divide(amount)
                .subtract(new BigDecimal("1"))
                .divide(new BigDecimal(term).divide(new BigDecimal("12")))
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        log.info("calculationPSK = " + psk);
        log.info("calculationPSK method end");
        return psk;
    }
}

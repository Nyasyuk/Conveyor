package conveyor.controller;

import conveyor.dto.CreditDTO;
import conveyor.dto.LoanApplicationRequestDTO;
import conveyor.dto.LoanOfferDTO;
import conveyor.dto.ScoringDataDTO;
import conveyor.exception.RefusalOfLoanException;
import conveyor.service.CreditService;
import conveyor.service.LoanOfferService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/conveyor")
@RestController
@Slf4j
public class ConveyorController {

    private final LoanOfferService loanOfferService;
    private final CreditService creditService;

    @Operation(summary = "Get Loan Offers LoanOfferDTO List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content =
            @Content(schema = @Schema(implementation = LoanOfferDTO.class)), description = "LoanOfferDTO List"),
            @ApiResponse(responseCode = "400", description = "Invalid LoanApplicationRequestDTO")
    })
    @PostMapping("/offers")
    public List<LoanOfferDTO> loanOffers(@RequestBody LoanApplicationRequestDTO loanApplicationRequestDTO) {
        log.info("input data: " + loanApplicationRequestDTO.toString());
        return loanOfferService.getLoanOffersCredit(loanApplicationRequestDTO);
    }

    @Operation(summary = "Get Calculation Credit CreditDTO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content =
            @Content(schema = @Schema(implementation = CreditDTO.class)), description = "CreditDTO"),
            @ApiResponse(responseCode = "400", description = "Invalid ScoringDataDTO")
    })
    @PostMapping("/calculation")
    public CreditDTO calculationCredit(@RequestBody ScoringDataDTO scoringDataDTO) throws RefusalOfLoanException {
        log.info("input data: " + scoringDataDTO.toString());
        return creditService.getCalculationCredit(scoringDataDTO);
    }
}
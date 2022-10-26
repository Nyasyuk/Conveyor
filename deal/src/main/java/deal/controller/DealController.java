package deal.controller;

import deal.dto.LoanApplicationRequestDTO;
import deal.dto.LoanOfferDTO;
import deal.service.LoanOfferService;
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
@RequestMapping("/deal")
@RestController
@Slf4j
public class DealController {

    private final LoanOfferService loanOfferService;

    @Operation(summary = "Get Loan Offers LoanOfferDTO List")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content =
            @Content(schema = @Schema(implementation = LoanOfferDTO.class)), description = "LoanOfferDTO List"),
            @ApiResponse(responseCode = "400", description = "Invalid LoanApplicationRequestDTO")
    })
    @PostMapping("/application")
    public List<LoanOfferDTO> loanOffers(@RequestBody LoanApplicationRequestDTO loanApplicationRequestDTO) {
        log.info("input data: " + loanApplicationRequestDTO.toString());
        return loanOfferService.getLoanOffersCredit(loanApplicationRequestDTO);
    }
}

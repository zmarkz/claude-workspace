package com.portfolio.controller;

import com.portfolio.dto.request.AddHoldingRequest;
import com.portfolio.dto.request.CreatePortfolioRequest;
import com.portfolio.dto.response.HoldingResponse;
import com.portfolio.dto.response.PortfolioAnalysisResponse;
import com.portfolio.dto.response.PortfolioResponse;
import com.portfolio.exception.ErrorResponse;
import com.portfolio.service.CsvUploadService;
import com.portfolio.service.PortfolioService;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolio Management")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final CsvUploadService csvUploadService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new portfolio",
               description = "Creates an empty portfolio for the specified user. Add holdings via the holdings endpoint or sync from Zerodha.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Portfolio created",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioResponse> createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        log.info("Creating portfolio for user: {}", request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.createPortfolio(request));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get portfolio by ID", description = "Returns portfolio details including all holdings with current prices.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portfolio found",
                    content = @Content(schema = @Schema(implementation = PortfolioResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioResponse> getPortfolio(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long id) {
        log.info("Fetching portfolio: {}", id);
        return ResponseEntity.ok(portfolioService.getPortfolio(id));
    }

    @GetMapping(value = "/{id}/analysis", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get portfolio analysis",
               description = "Returns computed metrics: total value, P&L, gain%, and sector allocation breakdown. Results are cached.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analysis computed",
                    content = @Content(schema = @Schema(implementation = PortfolioAnalysisResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PortfolioAnalysisResponse> getAnalysis(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long id) {
        log.info("Analyzing portfolio: {}", id);
        return ResponseEntity.ok(portfolioService.getPortfolioAnalysis(id));
    }

    @PostMapping(value = "/{id}/holdings", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a holding",
               description = "Adds a stock holding to the portfolio. If the stock already exists, quantities are merged with a weighted average buy price.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Holding added",
                    content = @Content(schema = @Schema(implementation = HoldingResponse.class))),
            @ApiResponse(responseCode = "404", description = "Portfolio or stock not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HoldingResponse> addHolding(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody AddHoldingRequest request) {
        log.info("Adding holding to portfolio: {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.addHolding(id, request));
    }

    @PostMapping(value = "/{id}/holdings/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload holdings from CSV",
               description = "Upload a CSV file (Zerodha Console format or generic: Symbol, Qty, Avg Price). Stocks not in the database are auto-created.")
    public ResponseEntity<Map<String, Object>> uploadHoldings(
            @Parameter(description = "Portfolio ID") @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "replace", defaultValue = "false") boolean replace) {
        log.info("Uploading CSV holdings to portfolio: {}, replace={}", id, replace);
        CsvUploadService.UploadResult result = csvUploadService.uploadHoldings(id, file, replace);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("synced", result.synced());
        response.put("created", result.created());
        response.put("total", result.total());
        response.put("errors", result.errors());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{portfolioId}/holdings/{holdingId}")
    @Operation(summary = "Remove a holding", description = "Permanently removes a holding from the portfolio.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Holding removed"),
            @ApiResponse(responseCode = "404", description = "Portfolio or holding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> removeHolding(
            @Parameter(description = "Portfolio ID", example = "1") @PathVariable Long portfolioId,
            @Parameter(description = "Holding ID", example = "5") @PathVariable Long holdingId) {
        log.info("Removing holding {} from portfolio: {}", holdingId, portfolioId);
        portfolioService.removeHolding(portfolioId, holdingId);
        return ResponseEntity.noContent().build();
    }
}

package com.portfolio.controller;

import com.portfolio.dto.kite.KiteHolding;
import com.portfolio.entity.Portfolio;
import com.portfolio.entity.PortfolioHolding;
import com.portfolio.entity.Stock;
import com.portfolio.entity.User;
import com.portfolio.exception.ErrorResponse;
import com.portfolio.repository.PortfolioHoldingRepository;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.StockRepository;
import com.portfolio.service.HoldingsSyncService;
import com.portfolio.service.KiteClient;
import com.portfolio.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kite")
@Tag(name = "Kite Connect")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class KiteAuthController {

    private final KiteClient kiteClient;
    private final UserService userService;
    private final HoldingsSyncService holdingsSyncService;
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;
    private final PortfolioHoldingRepository holdingRepository;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Redirect to Zerodha login",
            description = "Redirects the browser to the Kite Connect OAuth page. Open this URL directly in a browser — not via Swagger Try-it-out."
    )
    @ApiResponse(responseCode = "302", description = "Redirect to Kite OAuth login page")
    public ResponseEntity<Void> login() {
        if (!kiteClient.isConfigured()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(302)
                .location(URI.create(kiteClient.getLoginUrl()))
                .build();
    }

    @GetMapping("/callback")
    @SecurityRequirements
    @Operation(
            summary = "Kite OAuth callback",
            description = "Zerodha redirects here after login with a `request_token`. Exchanges it for an access token and redirects to the frontend."
    )
    @ApiResponse(responseCode = "302", description = "Redirects to frontend with kite_connected=true or kite_error=true")
    public ResponseEntity<Void> callback(
            @Parameter(description = "One-time request token from Zerodha", required = true)
            @RequestParam("request_token") String requestToken,
            @RequestParam(value = "action", required = false) String action) {
        try {
            String accessToken = kiteClient.generateSession(requestToken);
            return ResponseEntity.status(302)
                    .location(URI.create(frontendUrl + "/dashboard?kite_connected=true&token=" + accessToken))
                    .build();
        } catch (Exception e) {
            log.error("Kite OAuth callback failed", e);
            return ResponseEntity.status(302)
                    .location(URI.create(frontendUrl + "/dashboard?kite_error=true"))
                    .build();
        }
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check Kite connection status",
               description = "Returns whether the current user has a stored Kite access token.")
    @ApiResponse(responseCode = "200", description = "`{connected: true}` or `{connected: false}`")
    public ResponseEntity<Map<String, Object>> status(@AuthenticationPrincipal UserDetails userDetails) {
        boolean configured = kiteClient.isConfigured();
        if (!configured) {
            return ResponseEntity.ok(Map.of("connected", false, "configured", false));
        }
        User user = userService.getUserByEmail(userDetails.getUsername());
        boolean connected = user.getKiteAccessToken() != null && !user.getKiteAccessToken().isEmpty();
        return ResponseEntity.ok(Map.of("connected", connected, "configured", true));
    }

    @PostMapping(value = "/connect", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Store Kite access token",
            description = "Manually provide a Kite access token obtained from the Zerodha developer console. Useful for testing without the full OAuth flow."
    )
    @ApiResponse(responseCode = "200", description = "Token stored successfully")
    public ResponseEntity<Map<String, Object>> connect(
            @AuthenticationPrincipal UserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Access token from Kite Connect",
                    content = @Content(schema = @Schema(example = "{\"accessToken\": \"your_kite_access_token\"}")))
            @RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        User user = userService.getUserByEmail(userDetails.getUsername());
        user.setKiteAccessToken(accessToken);
        kiteClient.setAccessToken(accessToken);
        return ResponseEntity.ok(Map.of("connected", true));
    }

    @PostMapping(value = "/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Sync holdings from Zerodha",
            description = "Fetches your current Zerodha holdings via Kite Connect and upserts them into the specified portfolio. Creates stock records automatically if they don't exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync complete — returns count of synced and total holdings"),
            @ApiResponse(responseCode = "400", description = "Kite not connected or portfolio not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> syncHoldings(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Target portfolio ID to sync into", example = "1", required = true)
            @RequestParam Long portfolioId) {
        if (!kiteClient.isConfigured()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kite API keys not configured on server"));
        }

        User user = userService.getUserByEmail(userDetails.getUsername());

        if (user.getKiteAccessToken() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kite not connected"));
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Portfolio not found"));
        }

        int synced = holdingsSyncService.syncHoldingsForUser(user, portfolio);
        return ResponseEntity.ok(Map.of("synced", synced, "total", synced));
    }
}

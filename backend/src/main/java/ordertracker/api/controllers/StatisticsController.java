package ordertracker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ordertracker.core.services.VisitCounterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics API", description = "API for tracking and retrieving visit statistics")
public class StatisticsController {

    private final VisitCounterService visitCounterService;

    public StatisticsController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("single-stat")
    @Operation(
            summary = "Get visit count for specific URL",
            description = "Returns the number of visits for the specified URL"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved visit count")
    @ApiResponse(responseCode = "400", description = "Invalid URL parameter")
    public int getUrlVisitCount(
            @Parameter(description = "URL to get visit count for", required = true, example = "/api/products")
            @RequestParam String url
    ) {
        return visitCounterService.getCount(url);
    }

    @GetMapping
    @Operation(
            summary = "Get all visit counts",
            description = "Returns a map of all URLs with their corresponding visit counts"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all visit counts")
    public ConcurrentMap<String, Integer> getAllVisitCounts() {
        return visitCounterService.getAllCounts();
    }

    @GetMapping("/top-visited")
    @Operation(
            summary = "Get most visited URL",
            description = "Returns the most visited URL along with its visit count"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved most visited URL")
    @ApiResponse(responseCode = "404", description = "No visits recorded yet")
    public String getMostVisitedUrl() {
        ConcurrentHashMap<String, Integer> counts = visitCounterService.getAllCounts();
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> "Most visited URL: " + entry.getKey() + " (visits: " + entry.getValue() + ")")
                .orElse("No visits recorded yet");
    }
}
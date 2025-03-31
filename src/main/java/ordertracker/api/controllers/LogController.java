package ordertracker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log controller", description = "Controller for managing and viewing application logs")
public class LogController {

    private static final String LOG_FILE_PATH = "./OrderTracker.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(
            summary = "View logs by date",
            description = "Returns log entries filtered by the specified date in YYYY-MM-DD format as plain text"
    )
    @ApiResponse(responseCode = "200", description = "Log entries successfully retrieved and returned")
    @ApiResponse(responseCode = "400", description = "Invalid date format provided")
    @ApiResponse(responseCode = "404", description = "No logs found for the specified date or log file not found")
    @GetMapping("/view")
    public ResponseEntity<String> viewLogFile(
            @Parameter(
                    description = "Date in YYYY-MM-DD format to filter logs",
                    example = "2023-05-15",
                    required = true
            )
            @RequestParam(name = "date") String dateStr) throws IOException {

        // Проверка формата даты
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        // Проверка существования файла
        Path logPath = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(logPath)) {
            return ResponseEntity.notFound().build();
        }

        // Фильтрация логов по дате с использованием try-with-resources
        String filteredLogs;
        try (Stream<String> lines = Files.lines(logPath)) {
            filteredLogs = lines
                    .filter(line -> line.contains(dateStr))
                    .collect(Collectors.joining("\n"));
        }

        if (filteredLogs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(filteredLogs);
    }

    @Operation(
            summary = "Download logs by date",
            description = "Downloads log entries filtered by the specified date in YYYY-MM-DD format"
    )
    @ApiResponse(responseCode = "200", description = "Log file successfully generated and returned for download")
    @ApiResponse(responseCode = "400", description = "Invalid date format provided")
    @ApiResponse(responseCode = "404", description = "No logs found for the specified date or log file not found")
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(
                    description = "Date in YYYY-MM-DD format to filter logs",
                    example = "2023-05-15",
                    required = true
            )
            @RequestParam(name = "date") String dateStr) throws IOException {
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        // Check if file exists
        Path logPath = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(logPath)) {
            return ResponseEntity.notFound().build();
        }

        // Read and filter logs
        String filteredLogs;
        try (var lines = Files.lines(logPath)) {
            filteredLogs = lines
                    .filter(line -> line.contains(dateStr))
                    .collect(Collectors.joining("\n"));
        }

        if (filteredLogs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Create temp file for download
        Path tempLogFile = Files.createTempFile("logs-" + dateStr, ".log");
        Files.write(tempLogFile, filteredLogs.getBytes());

        Resource resource = new UrlResource(tempLogFile.toUri());
        tempLogFile.toFile().deleteOnExit();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs-" + dateStr + ".log")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
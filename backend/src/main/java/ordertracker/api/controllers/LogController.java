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
import ordertracker.core.models.LogTask;
import ordertracker.core.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log controller", description = "Controller for managing and viewing application logs")
public class LogController {

    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

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

        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }

        Path logPath = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(logPath)) {
            return ResponseEntity.notFound().build();
        }

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

        Path logPath = Paths.get(LOG_FILE_PATH);
        if (!Files.exists(logPath)) {
            return ResponseEntity.notFound().build();
        }

        String filteredLogs;
        try (var lines = Files.lines(logPath)) {
            filteredLogs = lines
                    .filter(line -> line.contains(dateStr))
                    .collect(Collectors.joining("\n"));
        }

        if (filteredLogs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path tempLogFile = Files.createTempFile("logs-" + dateStr, ".log");
        Files.write(tempLogFile, filteredLogs.getBytes());

        Resource resource = new UrlResource(tempLogFile.toUri());
        tempLogFile.toFile().deleteOnExit();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs-" + dateStr + ".log")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @Operation(summary = "Create a new log task", description = "Creates a new log generation task and returns its ID")
    @PostMapping
    public ResponseEntity<String> createLog() {
        LogTask task = logService.createLogTask();
        return ResponseEntity.ok(task.getId());
    }

    @Operation(summary = "Get task status", description = "Returns the current status of a log generation task")
    @ApiResponse(responseCode = "200", description = "Status returned successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getStatus(
            @Parameter(description = "ID of the log task", required = true)
            @PathVariable String id) {
        LogTask task = logService.getTask(id);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task.getStatus());
    }

    @Operation(summary = "Download log file", description = "Downloads the generated log file if the task is complete")
    @ApiResponse(responseCode = "200", description = "File returned successfully")
    @ApiResponse(responseCode = "404", description = "Task not found or file not ready")
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(
            @Parameter(description = "ID of the log task", required = true)
            @PathVariable String id) throws IOException {
        LogTask task = logService.getTask(id);
        if (task == null || !"READY".equals(task.getStatus())) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = task.getFilePath();
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);
    }
}
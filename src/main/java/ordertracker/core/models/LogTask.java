package ordertracker.core.models;

import java.nio.file.Path;
import java.util.UUID;
import lombok.Data;

@Data
public class LogTask {
    private final String id;
    private String status;
    private Path filePath;

    public LogTask() {
        this.id = UUID.randomUUID().toString();
        this.status = "CREATED";
    }
}

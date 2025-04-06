package ordertracker.apllication.components;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import ordertracker.core.models.LogTask;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncExecutor {
    @Async
    public void processTaskAsync(LogTask task) {
        try {
            task.setStatus("PROCESSING");
            Thread.sleep(300000);

            Path filePath = Paths.get("logs_" + task.getId() + ".txt");
            Files.write(filePath, ("Лог для " + task.getId()).getBytes());

            task.setFilePath(filePath);
            task.setStatus("READY");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus("CANCELLED");
        } catch (Exception e) {
            task.setStatus("FAILED");
        }
    }
}

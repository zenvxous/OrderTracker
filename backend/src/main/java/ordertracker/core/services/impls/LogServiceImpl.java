package ordertracker.core.services.impls;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ordertracker.apllication.components.AsyncExecutor;
import ordertracker.core.models.LogTask;
import ordertracker.core.services.LogService;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private final Map<String, LogTask> tasks = new ConcurrentHashMap<>();

    private final AsyncExecutor asyncExecutor;

    public LogServiceImpl(AsyncExecutor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public LogTask createLogTask() {
        LogTask task = new LogTask();
        tasks.put(task.getId(), task);
        asyncExecutor.processTaskAsync(task);
        return task;
    }

    @Override
    public LogTask getTask(String id) {
        return tasks.get(id);
    }
}

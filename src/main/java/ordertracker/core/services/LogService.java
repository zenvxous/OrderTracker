package ordertracker.core.services;

import ordertracker.core.models.LogTask;

public interface LogService {

    LogTask createLogTask();

    LogTask getTask(String id);
}

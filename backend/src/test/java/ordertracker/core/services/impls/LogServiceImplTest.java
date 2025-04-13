package ordertracker.core.services.impls;

import java.util.UUID;
import ordertracker.apllication.components.AsyncExecutor;
import ordertracker.core.models.LogTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LogServiceImplTest {

    @Mock
    private AsyncExecutor asyncExecutor;

    private LogServiceImpl logService;

    @BeforeEach
    void setUp() {
        logService = new LogServiceImpl(asyncExecutor);
    }

    @Test
    void getTask_shouldReturnNullWhenTaskNotExists() {
        // Arrange
        String nonExistentId = UUID.randomUUID().toString();

        // Act
        LogTask result = logService.getTask(nonExistentId);

        // Assert
        assertNull(result);
    }
}
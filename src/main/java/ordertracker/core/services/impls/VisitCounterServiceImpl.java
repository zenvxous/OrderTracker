package ordertracker.core.services.impls;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import ordertracker.core.services.VisitCounterService;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterServiceImpl implements VisitCounterService {
    private final ConcurrentHashMap<String, LongAdder> urlCounter = new ConcurrentHashMap<>();

    @Override
    public void incrementCount(String url) {
        LongAdder adder = urlCounter.computeIfAbsent(url, k -> new LongAdder());
        adder.increment();  // Намного быстрее AtomicInteger при высокой нагрузке
    }

    @Override
    public int getCount(String url) {
        LongAdder adder = urlCounter.get(url);
        return adder == null ? 0 : adder.intValue();
    }

    @Override
    public ConcurrentHashMap<String, Integer> getAllCounts() {
        ConcurrentHashMap<String, Integer> result = new ConcurrentHashMap<>();
        urlCounter.forEach((url, adder) -> result.put(url, adder.intValue()));
        return result;
    }
}

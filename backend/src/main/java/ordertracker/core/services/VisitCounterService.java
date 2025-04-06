package ordertracker.core.services;

import java.util.concurrent.ConcurrentHashMap;

public  interface VisitCounterService {

    void incrementCount(String url);

    int getCount(String url);

    ConcurrentHashMap<String, Integer> getAllCounts();
}

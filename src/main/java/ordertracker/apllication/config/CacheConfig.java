package ordertracker.apllication.config;

import ordertracker.apllication.cache.InMemoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public InMemoryCache<Integer, Object> customerCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Integer, Object> mealCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Integer, Object> orderCache() {
        return new InMemoryCache<>();
    }
}

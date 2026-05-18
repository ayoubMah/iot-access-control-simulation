package upec.badge.cache_loader_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("upec.badge.shared.model")
public class CacheLoaderBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheLoaderBackendApplication.class, args);
    }

}
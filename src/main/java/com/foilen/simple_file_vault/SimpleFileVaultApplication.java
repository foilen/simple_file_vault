package com.foilen.simple_file_vault;

import com.foilen.simple_file_vault.config.Config;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RegisterReflectionForBinding({Config.class})
public class SimpleFileVaultApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleFileVaultApplication.class, args);
    }

}

package com.foilen.simple_file_vault;

import com.foilen.simple_file_vault.config.Config;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@RegisterReflectionForBinding({Config.class})
public class NativeConfig {
}

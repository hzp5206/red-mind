package com.redmind.modules.setting.config;

import com.redmind.modules.setting.service.AiSettingService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AiSettingRuntimeInitializer implements ApplicationRunner {

    private final AiSettingService aiSettingService;

    public AiSettingRuntimeInitializer(AiSettingService aiSettingService) {
        this.aiSettingService = aiSettingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        aiSettingService.refreshRuntimeProperties();
    }
}

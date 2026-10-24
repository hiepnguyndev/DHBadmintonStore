package com.badminton.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@org.springframework.context.annotation.Configuration
@ConditionalOnProperty(prefix = "app.scheduling", name = "enabled", havingValue = "true", matchIfMissing = true)
@org.springframework.scheduling.annotation.EnableScheduling
public class SchedulingConfig {}

package com.github.ddm4j.api.document.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication // web应用才生效
@ComponentScan(basePackages = { "com.github.ddm4j.api.document.controller", "com.github.ddm4j.api.document.check",
		"com.github.ddm4j.api.document.config" })
public class ApiDocumentConfig {

}

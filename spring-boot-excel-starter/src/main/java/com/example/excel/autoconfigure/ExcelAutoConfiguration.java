package com.example.excel.autoconfigure;

import com.alibaba.excel.EasyExcel;

import com.example.excel.core.DefaultExcelTemplate;
import com.example.excel.core.ExcelTemplate;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration that exposes an {@link ExcelTemplate} bean when EasyExcel is on
 * the classpath and the starter is enabled ({@code excel.enabled=true}, the default).
 */
@AutoConfiguration
@ConditionalOnClass(EasyExcel.class)
@EnableConfigurationProperties(ExcelProperties.class)
@ConditionalOnProperty(prefix = ExcelProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExcelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ExcelTemplate.class)
    public ExcelTemplate excelTemplate(ExcelProperties properties) {
        return new DefaultExcelTemplate(properties);
    }
}

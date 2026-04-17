package com.backandwhite;

import com.backandwhite.config.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestMicProductcategoryApplication {

    public static void main(String[] args) {
        SpringApplication.from(MicProductcategoryApplication::main).with(TestContainersConfiguration.class).run(args);
    }
}

package io.github.geancgbr.orders;

import org.springframework.boot.SpringApplication;

public class TestDddHexagonalApplication {

    public static void main(String[] args) {
        SpringApplication.from(DddHexagonalApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}

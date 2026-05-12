package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableRetry
@EnableScheduling
public class StsPortfolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(StsPortfolioApplication.class, args);
	}

}

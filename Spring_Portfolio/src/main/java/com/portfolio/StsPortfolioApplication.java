package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ServletComponentScan
@EnableRetry
public class StsPortfolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(StsPortfolioApplication.class, args);
	}

}

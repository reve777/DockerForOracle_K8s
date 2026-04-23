package com.portfolio.uitls;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

import com.portfolio.aspect.ByteSize;

/**
 * 字節長度驗證器，使用UTF-8
 */
public class ByteSizeValidator implements ConstraintValidator<ByteSize, String> {
	private int max;
	private int min;

	@Override
	public void initialize(ByteSize annotation) {
		this.max = annotation.max();
		this.min = annotation.min();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		int byteLength = value.getBytes(StandardCharsets.UTF_8).length;

		// 如果 min 是預設值 0，則只檢查上限
		if (min == 0) {
			return byteLength <= max;
		}

		// 否則檢查上下限
		return byteLength >= min && byteLength <= max;
	}
}
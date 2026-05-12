package com.portfolio.linepay.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LinePaySignatureUtil {
	public static String encrypt(String channelSecret, String uri, String queryOrBody, String nonce) {
		try {
			String message = channelSecret + uri + queryOrBody + nonce;
			SecretKeySpec keySpec = new SecretKeySpec(channelSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(keySpec);
			byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("產生簽章失敗", e);
		}
	}
}
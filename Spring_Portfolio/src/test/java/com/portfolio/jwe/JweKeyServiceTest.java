package com.portfolio.jwe;

import com.portfolio.jwe.dto.JweKeyRequest;
import com.portfolio.jwe.dto.JweKeyResponse;
import com.portfolio.jwe.service.impl.JweKeyServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class JweKeyServiceTest {

    @Test
    public void testCreateAndPrintSelfAndOrg() throws Exception {
        com.portfolio.jwe.config.JweKeyProperties props = new com.portfolio.jwe.config.JweKeyProperties();
        // defaults are fine (SLF / OGN)
        JweKeyServiceImpl service = new JweKeyServiceImpl(props);

        // 1) create SELF
        JweKeyRequest selfReq = new JweKeyRequest();
        selfReq.setKeyType("SLF");
        JweKeyResponse selfResp = service.create(selfReq);

        System.out.println("=== SELF Key Created ===");
        System.out.println("keyId: " + selfResp.getKeyId());
        System.out.println("keyRef: " + selfResp.getKeyRef());
        System.out.println("pubKeyBase64 length: " + (selfResp.getPubKeyBase64() == null ? 0 : selfResp.getPubKeyBase64().length()));
        System.out.println("priKeyBase64 length: " + (selfResp.getPriKeyBase64() == null ? 0 : selfResp.getPriKeyBase64().length()));

        // 2) create a PEM file from the generated public key and save to test resources
        String pubBase64 = selfResp.getPubKeyBase64();
        assertNotNull(pubBase64);

        // chunk base64 into 64-char lines for PEM readability
        String chunked = pubBase64.codePoints()
                .mapToObj(c -> String.valueOf((char)c))
                .collect(Collectors.joining());

        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < pubBase64.length(); i += 64) {
            int end = Math.min(i + 64, pubBase64.length());
            pemBuilder.append(pubBase64, i, end).append('\n');
        }
        pemBuilder.append("-----END PUBLIC KEY-----\n");

        Path pemPath = Paths.get("src/test/resources/test_jwe_pub.pem");
        Files.createDirectories(pemPath.getParent());
        Files.write(pemPath, pemBuilder.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote PEM to: " + pemPath.toAbsolutePath());

        // 3) call ORG flow using the created PEM as MultipartFile
        byte[] pemBytes = pemBuilder.toString().getBytes(StandardCharsets.UTF_8);
        MockMultipartFile mockFile = new MockMultipartFile("file", "test_jwe_pub.pem", "application/x-pem-file", pemBytes);

        JweKeyRequest orgReq = new JweKeyRequest();
        orgReq.setKeyType("OGN");
        orgReq.setFile(mockFile);
        JweKeyResponse orgResp = service.create(orgReq);

        System.out.println("=== ORG Key Processed ===");
        System.out.println("keyId: " + orgResp.getKeyId());
        System.out.println("keyRef: " + orgResp.getKeyRef());
        System.out.println("pubKeyBase64 length: " + (orgResp.getPubKeyBase64() == null ? 0 : orgResp.getPubKeyBase64().length()));

        // 4) create and print a demo license file content
        Path licPath = Paths.get("src/test/resources/demo.license");
        if (Files.exists(licPath)) {
            String license = Files.readString(licPath, StandardCharsets.UTF_8);
            System.out.println("=== demo.license content ===\n" + license);
        } else {
            String sample = "DEMO_LICENSE=This is a sample license for tests\nKEYREF=" + orgResp.getKeyRef();
            Files.write(licPath, sample.getBytes(StandardCharsets.UTF_8));
            System.out.println("Wrote demo license to: " + licPath.toAbsolutePath());
            System.out.println("=== demo.license content ===\n" + sample);
        }
    }
}

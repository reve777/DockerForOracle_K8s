package com.portfolio.license;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

/**
 * Standalone license generator.
 * Produces a license binary which is: [0x4C,0x01] + IV(16) + AES-CBC(encrypted public key) + licenseRef(8)
 *
 * 中文說明:
 * 此服務負責產生一組 license，流程摘要如下：
 * 1) 產生 RSA 金鑰對 (2048-bit)
 * 2) 產生 activationCode（4 random bytes -> 8 hex chars）
 * 3) 以私鑰的 SHA-256 前 8 bytes 作為 licenseRef
 * 4) 將 public key 加密（使用 AES-CBC，key = SHA256(salt || activationCode)），
 *    並組合成 license binary：header(0x4C,0x01) + IV(16) + encryptedPublicKey + licenseRef(8)
 *
 * 回傳的 LicenseResult 包含 base64 編碼的 license binary、activationCode、
 * licenseRef 的 base64，以及 privateKey 的 base64（用於備份，請安全保存）。
 */
@Service
public class LicenseGeneratorService {

    // project-specific salt (keep private/unique in production)
    // 中文: 專案專用的 salt，用於派生 AES key。生產環境請改為更安全/不可公開的值。
    private static final String SALT = "portfolioSalt";
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    private static final String SHA256 = "SHA-256";

    public LicenseResult generateLicense() throws Exception {
        // 1. generate RSA keypair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 2. generate activation code (4 random bytes -> 8 hex chars)
        byte[] activateCodeRaw = new byte[4];
        SecureRandom random = new SecureRandom();
        random.nextBytes(activateCodeRaw);
        String activationCode = bytesToHex(activateCodeRaw).toUpperCase();

        // 3. licenseRef = first 8 bytes of SHA-256(privateKey)
        MessageDigest sha256 = MessageDigest.getInstance(SHA256);
        byte[] privKeyBytes = privateKey.getEncoded();
        byte[] privKeyHash = sha256.digest(privKeyBytes);
        byte[] licenseRefBytes = new byte[8];
        System.arraycopy(privKeyHash, 0, licenseRefBytes, 0, 8);

        // 4. create license binary
        byte[] licenseData = buildLicense(publicKey, activationCode, licenseRefBytes);

        LicenseResult result = new LicenseResult();
        result.setLicenseBase64(Base64.getEncoder().encodeToString(licenseData));
        result.setActivationCode(activationCode);
        result.setLicenseRefBase64(Base64.getEncoder().encodeToString(licenseRefBytes));
        result.setPrivateKeyBase64(Base64.getEncoder().encodeToString(privKeyBytes));

        return result;
    }

    private byte[] buildLicense(PublicKey publicKey, String activationCode, byte[] licenseRef) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // header
        out.write(0x4C);
        out.write(0x01);

        // IV
        byte[] iv = new byte[16];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(iv);
        out.write(iv);

        // derive AES-256 key: SHA256(salt || activationCode)
        MessageDigest digest = MessageDigest.getInstance(SHA256);
        digest.update(SALT.getBytes(StandardCharsets.US_ASCII));
        digest.update(activationCode.getBytes(StandardCharsets.US_ASCII));
        byte[] aesKey = digest.digest(); // 32 bytes

        // encrypt public key with AES-CBC
        byte[] encodePublicKey = publicKey.getEncoded();
        Cipher cipher = Cipher.getInstance(AES_MODE);
        SecretKeySpec key = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encryptedPublicKey = cipher.doFinal(encodePublicKey);
        out.write(encryptedPublicKey);

        // licenseRef (8 bytes)
        out.write(licenseRef);

        return out.toByteArray();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }
}

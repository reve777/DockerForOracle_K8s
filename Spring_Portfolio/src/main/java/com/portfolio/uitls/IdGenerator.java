package com.portfolio.uitls;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.github.f4b6a3.uuid.UuidCreator;

@Component
public class IdGenerator {

    /**
     * 使用 UUIDv7 產生唯一識別碼
     * UUIDv7 基於時間戳記並提供更好的效能和唯一性
     * @return 新的唯一 UUID 字串
     */
    public String generateUniqueId() {
        // 使用 UUIDv7 函式庫產生 UUID
        UUID uuid = UuidCreator.getTimeOrderedEpoch();
        return uuid.toString();
    }

    /**x
     * 產生格式化的 UUIDv7，移除連接符號
     * @return 無連接符號的 UUID 字串
     */
    public String generateCompactUniqueId() {
        return generateUniqueId().replace("-", "");
    }

    /**
     * 產生具有特定前綴的 UUIDv7
     * @param prefix 需要加在 UUID 前的前綴字串
     * @return 帶有前綴的 UUID 字串
     */
    public String generatePrefixedUniqueId(String prefix) {
        return prefix + generateCompactUniqueId();
    }

}

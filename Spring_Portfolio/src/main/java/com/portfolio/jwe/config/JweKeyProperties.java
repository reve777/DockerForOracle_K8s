package com.portfolio.jwe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configurable key type codes. Defaults chosen so you can change without code edits.
 */
@Component
@ConfigurationProperties(prefix = "jwe.keytype")
public class JweKeyProperties {
    /**
     * code for self-generated key (default SLF)
     */
    private String self = "SLF";
    /**
     * code for organisation uploaded key (default OGN)
     */
    private String org = "OGN";

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public boolean isSelfCode(String code) {
        if (code == null) return false;
        String c = code.trim();
        return c.equalsIgnoreCase(self) || c.equalsIgnoreCase("SELF");
    }

    public boolean isOrgCode(String code) {
        if (code == null) return false;
        String c = code.trim();
        return c.equalsIgnoreCase(org) || c.equalsIgnoreCase("ORG");
    }
}

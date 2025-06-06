package com.ctrip.framework.drc.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class EncryptUtilsTest {
    
    
    @Test
    public void test() throws Exception {
        String rawToken = EncryptUtils.encryptAES_ECB("token", "appid");
        String s = EncryptUtils.decryptAES_ECB(rawToken, "appid");
        Assert.assertEquals("token",s);
    }

    @Test
    public void generateRawToken() throws Exception {
        String rawToken = EncryptUtils.encryptAES_ECB("token", "appid");
    }
    
    
    
}
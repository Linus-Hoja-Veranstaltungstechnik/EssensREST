package net.alphalightning.rest.util;

import net.alphalightning.rest.auth.ApiKey;

import java.util.Random;

public class ApiKeyUtils {
    private static final String API_KEY_PATTERN = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";

    public static ApiKey generateApiKey(String identifier) {
        char[] apiKeyChars = API_KEY_PATTERN.toCharArray();
        for (int i = 0; i < apiKeyChars.length; i++) {
            char currentChar = apiKeyChars[i];
            if (currentChar == 'x') apiKeyChars[i] = randomHexChar();
        }
        return new ApiKey(identifier, new String(apiKeyChars));
    }

    private static char randomHexChar(){
        Random random = new Random();
        int hexValue = (int) Math.floor(random.nextFloat()*16);
        return Integer.toHexString(hexValue).charAt(0);
    }

}

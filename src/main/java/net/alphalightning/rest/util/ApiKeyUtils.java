package net.alphalightning.rest.util;

import java.util.Random;

public class ApiKeyUtils {
    private static final String API_KEY_PATTERN = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
    private static final int API_KEY_DEFAULT_LENGTH = "59dd2339-6519-4e11-a24d-5e4ba6e6a91d".length();

    public static String generateApiKey(){
        char[] apiKeyChars = API_KEY_PATTERN.toCharArray();
        for(int i=0; i< apiKeyChars.length; i++){
            char currentChar = apiKeyChars[i];
            if(currentChar == 'x') apiKeyChars[i] = randomHexChar();
        }
        return new String(apiKeyChars);
    }

    private static char randomHexChar(){
        Random random = new Random();
        int hexValue = (int) Math.floor(random.nextFloat()*16);
        return Integer.toHexString(hexValue).charAt(0);
    }

}

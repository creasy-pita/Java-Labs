package com.creasypita;


import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Random;

/**
 * AES加密解密
 */
public class AESUtils {


//    private static Logger logger = LoggerFactory.getLogger(AESUtils.class);

    public static byte[] AES_CBC_Decrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, key, iv);
        return cipher.doFinal(data);
    }

    public static byte[] AES_CBC_Encrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(data);
    }

    private static Cipher getCipher(int mode, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(mode, secretKeySpec, new IvParameterSpec(iv));

        return cipher;
    }

    public static void main(String[] args) {
        ArrayList enCryptStrList = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            enCryptStrList.add(encrypt(givenUsingJava8_whenGeneratingRandomAlphanumericString_thenCorrect(), "gisq39561c9fe068"));
        }
        for (Object str : enCryptStrList) {
            System.out.println("enCryptStr:" + str.toString());
            System.out.println(decrypt(str.toString(), "gisq39561c9fe068"));
        }

    }

    public static String givenUsingJava8_whenGeneratingRandomAlphanumericString_thenCorrect() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 120;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        System.out.println(generatedString);
        return generatedString;
    }

    /**
     * 加密
     *
     * @param content     待解密内容
     * @param expandedKey 秘钥
     * @return
     */
    public static String encrypt(String content, String expandedKey) {
        return encrypt(content, expandedKey, expandedKey);
    }

    public static String encrypt(String content, String expandedKey, String expandedIv) {
        try {
            byte[] data = content.getBytes("UTF-8");
            byte[] key = expandedKey.getBytes("UTF-8");
            byte[] iv = expandedIv.getBytes("UTF-8");
            try {
                byte[] json = AES_CBC_Encrypt(data, key, iv);
                return Hex.encodeHexString(json);
            } catch (Exception e) {
                // logger.error(e.getMessage());
            }
        } catch (Exception e) {
            // logger.error(e.getMessage());
        }
        return null;
    }

    public static String encryptBase64(String content, String expandedKey, String expandedIv) {
        try {
            byte[] data = content.getBytes("UTF-8");
            byte[] key = expandedKey.getBytes("UTF-8");
            byte[] iv = expandedIv.getBytes("UTF-8");
            try {
                byte[] json = AES_CBC_Encrypt(data, key, iv);
                return encryptBase64(json);
            } catch (Exception e) {
                // logger.error(e.getMessage());
            }
        } catch (Exception e) {
            // logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content     待解密内容
     * @param expandedKey 秘钥
     * @return
     */
    public static String decrypt(String content, String expandedKey) {
        try {
            byte[] data = Hex.decodeHex(content.toCharArray());
            byte[] key = expandedKey.getBytes();
            try {
                byte[] json = AES_CBC_Decrypt(data, key, key);
                return new String(json);
            } catch (Exception e) {
                // logger.error(e.getMessage());
            }
        } catch (Exception e) {
            // logger.error(e.getMessage());
        }

        return null;
    }


    public static String decrypt(String content, String expandedKey, String expandedIv) {
        try {
            byte[] data = Hex.decodeHex(content.toCharArray());
            byte[] key = expandedKey.getBytes("UTF-8");
            byte[] iv = expandedIv.getBytes("UTF-8");
            try {
                byte[] json = AES_CBC_Decrypt(data, key, iv);
                return new String(json);
            } catch (Exception e) {
//                // logger.error(e.getMessage());
            }
        } catch (Exception e) {
//            // logger.error(e.getMessage());
        }

        return null;
    }

    public static String decryptBase64(String content, String expandedKey, String expandedIv) {
        try {
            byte[] data =decryptBase64(content);
            byte[] key = expandedKey.getBytes("UTF-8");
            byte[] iv = expandedIv.getBytes("UTF-8");
            try {
                byte[] json = AES_CBC_Decrypt(data, key, iv);
                return new String(json);
            } catch (Exception e) {
                // logger.error(e.getMessage());
            }
        } catch (Exception e) {
            // logger.error(e.getMessage());
        }

        return null;
    }

    /**
     * BASE64 解密
     *
     * @param key 需要解密的字符串
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] decryptBase64(String key) {
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(key);
    }

    /**
     * BASE64 加密
     *
     * @param key 需要加密的字节数组
     * @return 字符串
     * @throws Exception
     */
    public static String encryptBase64(byte[] key) {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(key);
    }
}


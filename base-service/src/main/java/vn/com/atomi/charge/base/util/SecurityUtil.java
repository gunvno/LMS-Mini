package vn.com.atomi.charge.base.util;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import vn.com.atomi.charge.base.model.constant.Algorithm;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class SecurityUtil {

    public SecurityUtil() {
    }

    public static SecurityUtil getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public static PrivateKey readPrivateKey(String privateKeyContent) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA_KEYPAIR, "BC");
            privateKeyContent = privateKeyContent.replace("\\r", "")
                    .replace("\\n", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace(" ", "");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
            return keyFactory.generatePrivate(keySpecPKCS8);
        } catch (Exception e) {
            return null;
        }
    }

    public static RSAPublicKey readPublicKey(String publicKeyContent) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyFactory keyFactory = KeyFactory.getInstance(Algorithm.RSA_KEYPAIR, "BC");
            publicKeyContent = publicKeyContent.replace("\\r", "")
                    .replace("\\n", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace(" ", "");
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            return (RSAPublicKey) keyFactory.generatePublic(keySpecX509);
        } catch (Exception e) {
            log.error("readPublicKey error: {}", Util.beautyError(e));
            return null;
        }
    }

    public static String encrypt(String rawText, Key key) {
        try {
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance(Algorithm.RSA_ENCRYPT, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(rawText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String cipherText, Key key) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(Algorithm.RSA_ENCRYPT, "BC");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)), StandardCharsets.UTF_8);
    }

    public static boolean verifySignRSA(String msg, String original, PublicKey key, String algorithm)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature sig = Signature.getInstance(algorithm);
        sig.initVerify(key);
        sig.update(original.getBytes());
        return sig.verify(Base64.getDecoder().decode(msg.getBytes()));
    }

    public static String signData(String data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(Algorithm.SHA256withRSA);
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] digitalSignature = signature.sign();
        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    public static boolean verifySignature(String data, String signatureStr, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(Algorithm.SHA256withRSA);
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] digitalSignature = Base64.getDecoder().decode(signatureStr);
        return signature.verify(digitalSignature);
    }

    public static boolean verifySignature(String data, String signatureStr, String publicKey) {
      try {
        Signature signature = Signature.getInstance(Algorithm.SHA256withRSA);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        PublicKey publicKeyRSA = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        signature.initVerify(publicKeyRSA);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] digitalSignature = Base64.getDecoder().decode(signatureStr);
        return signature.verify(digitalSignature);
      } catch (Exception e) {
        return false;
      }
    }

    public static String genHmac(String message, String secretKey, String algorithm) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            byte[] hexArray = {(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'};
            byte[] hexChars = new byte[rawHmac.length * 2];
            for (int j = 0; j < rawHmac.length; j++) {
                int v = rawHmac[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String genHashData(String data, String algorithm) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(data.getBytes());
            for (byte hashData : hash) {
                stringBuilder.append(String.format("%02x", hashData));
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String genSignRSA(String msg, PrivateKey key, String algorithm)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        byte[] dataByte = msg.getBytes();
        Signature sig = Signature.getInstance(algorithm);
        sig.initSign(key);
        sig.update(dataByte);
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    public static String createOtp(int length) {
        if (length > 0 && length <= 10) {
            String input = DateUtil.genUnixTimeString();
            String hash = genHashData(input, Algorithm.SHA256);
            int offset = hash.length() - 8;
            String hexSegment = hash.substring(Math.max(0, offset));
            long numericValue = Long.parseUnsignedLong(hexSegment, 16);
            int otpMod = (int) Math.pow(10.0F, length);
            int otp = (int) (numericValue % (long) otpMod);
            return String.format("%0" + length + "d", otp);
        } else {
            throw new IllegalArgumentException("OTP length must be between 1 and 10");
        }
    }

    public static String encryptStandard(String content, String key, String algorithm) {
        String encryptedString = null;
        try {
            byte[] arrayBytes = key.getBytes(StandardCharsets.UTF_8);
            KeySpec ks = new DESedeKeySpec(arrayBytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            Key secretKey = skf.generateSecret(ks);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] plainText = content.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedText = cipher.doFinal(plainText);
            encryptedString = Base64.getEncoder().encodeToString(encryptedText);
        } catch (Exception e) {
            log.error("encryptStandard error: {}", Util.beautyError(e));
        }
        return encryptedString;
    }

    public static String decryptStandard(String encrypted, String key, String algorithm) {
        String decryptedText = null;
        try {
            byte[] arrayBytes = key.getBytes(StandardCharsets.UTF_8);
            KeySpec ks = new DESedeKeySpec(arrayBytes);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            Key secretKey = skf.generateSecret(ks);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedText = Base64.getDecoder().decode(encrypted);
            byte[] plainText = cipher.doFinal(encryptedText);
            decryptedText = new String(plainText);
        } catch (Exception e) {
            log.error("decryptStandard error: {}", Util.beautyError(e));
        }
        return decryptedText;
    }

    public static String genJwtToken(Map<String, Object> jwtData, JWSAlgorithm algorithm, String signKey) {
        try {
            JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
            Objects.requireNonNull(jwtData).forEach(claimsSet::claim);
            JWSSigner signer = new RSASSASigner(Objects.requireNonNull(readPrivateKey(signKey)));
            JWSHeader jwsHeader = new JWSHeader.Builder(algorithm).type(JOSEObjectType.JWT).build();
            SignedJWT signedJWT = new SignedJWT(jwsHeader, claimsSet.build());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("genJwtToken error: {}", Util.beautyError(e));
            return null;
        }
    }

    public static String sha256(String input) {
      try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
      } catch (Exception e) {
        return null;
      }
    }

    public static class SingletonHelper {
        private static final SecurityUtil INSTANCE = new SecurityUtil();
    }

}
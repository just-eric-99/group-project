package util;

//import org.apache.commons.codec.binary.Hex;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDSAUtils {

    //generate KeyPair
    public static KeyPair getKeyPair() throws Exception {
        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");

        //Initializing the KeyPairGenerator
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        keyPairGen.initialize(256, random);

        return keyPairGen.generateKeyPair();
    }


    //generate signature
    public static String signECDSA(PrivateKey privateKey, String message) {
        String result = "";
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes());

            byte[] sign = signature.sign();

            //System.out.println("ECDSA signature: " + Hex.encodeHexString(sign));
            //return Hex.encodeHexString(sign);
            //System.out.println("ECDSA signature: " + DatatypeConverter.printHexBinary(sign));
            return DatatypeConverter.printHexBinary(sign);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //verify signature
    public static boolean verifyECDSA(PublicKey publicKey, String signed, String message) {
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());

            //byte[] hex = Hex.decodeHex(signed);
            byte[] hex = DatatypeConverter.parseHexBinary(signed);
            boolean bool = signature.verify(hex);

            System.out.println("verify：" + bool);
            return bool;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static Key stringToKey(String str, boolean isPrivate) throws GeneralSecurityException, IOException {
        byte[] data = Base64.getDecoder().decode(str.getBytes());
        EncodedKeySpec spec = null;
        if (isPrivate) {
            spec = new PKCS8EncodedKeySpec(data);
        } else {
            spec = new X509EncodedKeySpec(data);
        }
        KeyFactory fact = KeyFactory.getInstance("EC");

        return fact.generatePublic(spec);
    }
}

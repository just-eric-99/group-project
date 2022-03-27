package util;

import java.io.*;
import java.security.*;

/**
 * RSA 签名验签工具类
 */
public class RSASignUtils {

    /** 秘钥对算法名称 */
    private static final String ALGORITHM = "RSA";

    /** 密钥长度 */
    private static final int KEY_SIZE = 2048;

    /** 签名算法 */
    private static final String SIGNATURE_ALGORITHM = "Sha1WithRSA";

    /**
     * 随机生成 RSA 密钥对（包含公钥和私钥）
     */
    public static KeyPair generateKeyPair() throws Exception {
        // 获取指定算法的密钥对生成器
        KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);

        // 初始化密钥对生成器（指定密钥长度, 使用默认的安全随机数源）
        gen.initialize(KEY_SIZE);

        // 随机生成一对密钥（包含公钥和私钥）
        return gen.generateKeyPair();
    }

    /**
     * 私钥签名（数据）: 用私钥对指定字节数组数据进行签名, 返回签名信息
     */
    public static byte[] sign(byte[] data, PrivateKey priKey) throws Exception {
        // 根据指定算法获取签名工具
        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);

        // 用私钥初始化签名工具
        sign.initSign(priKey);

        // 添加要签名的数据
        sign.update(data);

        // 计算签名结果（签名信息）
        byte[] signInfo = sign.sign();

        return signInfo;
    }

    /**
     * 公钥验签（数据）: 用公钥校验指定数据的签名是否来自对应的私钥
     */
    public static boolean verify(byte[] data, byte[] signInfo, PublicKey pubKey) throws Exception {
        // 根据指定算法获取签名工具
        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);

        // 用公钥初始化签名工具
        sign.initVerify(pubKey);

        // 添加要校验的数据
        sign.update(data);

        // 校验数据的签名信息是否正确,
        // 如果返回 true, 说明该数据的签名信息来自该公钥对应的私钥,
        // 同一个私钥的签名, 数据和签名信息一一对应, 只要其中有一点修改, 则用公钥无法校验通过,
        // 因此可以用私钥签名, 然后用公钥来校验数据的完整性与签名者（所有者）
        boolean verify = sign.verify(signInfo);

        return verify;
    }

    /**
     * 私钥签名（文件）: 用私钥对文件进行签名, 返回签名信息
     */
    public static byte[] signFile(File file, PrivateKey priKey) throws Exception {
        // 根据指定算法获取签名工具
        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);

        // 用私钥初始化签名工具
        sign.initSign(priKey);

        InputStream in = null;

        try {
            in = new FileInputStream(file);

            byte[] buf = new byte[1024];
            int len = -1;

            while ((len = in.read(buf)) != -1) {
                // 添加要签名的数据
                sign.update(buf, 0, len);
            }

        } finally {
            close(in);
        }

        // 计算并返回签名结果（签名信息）
        return sign.sign();
    }

    /**
     * 公钥验签（文件）: 用公钥校验指定文件的签名是否来自对应的私钥
     */
    public static boolean verifyFile(File file, byte[] signInfo, PublicKey pubKey) throws Exception {
        // 根据指定算法获取签名工具
        Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);

        // 用公钥初始化签名工具
        sign.initVerify(pubKey);

        InputStream in = null;

        try {
            in = new FileInputStream(file);

            byte[] buf = new byte[1024];
            int len = -1;

            while ((len = in.read(buf)) != -1) {
                // 添加要校验的数据
                sign.update(buf, 0, len);
            }

        } finally {
            close(in);
        }

        // 校验签名
        return sign.verify(signInfo);
    }

    private static void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // nothing
            }
        }
    }

}

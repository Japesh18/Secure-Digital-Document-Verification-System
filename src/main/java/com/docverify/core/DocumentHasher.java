package com.docverify.core;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.HexFormat;
public class DocumentHasher
{
    public enum Algorithm
    {
        SHA_256("SHA-256"),
        SHA_512("SHA-512");
        final String jcaName;
        Algorithm(String name) { this.jcaName = name; }
    }
    public static String hashFile(Path filePath, Algorithm algorithm) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest digest=MessageDigest.getInstance(algorithm.jcaName);
        try (InputStream is=new BufferedInputStream(Files.newInputStream(filePath)))
        {
            byte[] buffer=new byte[8192];
            int bytesRead;
            while((bytesRead=is.read(buffer))!=-1)
            {
                digest.update(buffer,0,bytesRead);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    public static String hashBytes(byte[] data,Algorithm algorithm) throws NoSuchAlgorithmException
    {
        MessageDigest digest=MessageDigest.getInstance(algorithm.jcaName);
        digest.update(data);
        return HexFormat.of().formatHex(digest.digest());
    }
    public static boolean verifyFile(Path filePath,String expectedHash,Algorithm algorithm)
            throws IOException,NoSuchAlgorithmException
            {
                String actualHash=hashFile(filePath,algorithm);
                return MessageDigest.isEqual(actualHash.getBytes(),expectedHash.getBytes());
            }
    public static boolean verifyBytes(byte[] data,String expectedHash,Algorithm algorithm)
            throws NoSuchAlgorithmException
            {
                String actualHash=hashBytes(data,algorithm);
                return MessageDigest.isEqual(actualHash.getBytes(),expectedHash.getBytes());
            }
}
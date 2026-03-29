package com.docverify;
import com.docverify.core.DocumentHasher;
import com.docverify.core.DocumentHasher.Algorithm;
import com.docverify.model.DocumentRecord;
import com.docverify.service.DocumentService;
import com.docverify.service.DocumentService.VerificationResult;
import com.docverify.storage.DocumentStore;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocVerifyTest
{
    static Path testFile,tamperedFile,storeFile;
    static DocumentService service;
    static String registeredId;
    @BeforeAll
    static void setup() throws Exception
    {
        testFile=Files.createTempFile("docverify_test_",".txt");
        tamperedFile=Files.createTempFile("docverify_tampered_",".txt");
        storeFile=Files.createTempFile("docverify_store_",".csv");
        Files.delete(storeFile);
        Files.writeString(testFile,"This is an authentic document. Roll No: 2021-CS-001.");
        Files.writeString(tamperedFile,"This is an authentic document. Roll No: 2021-CS-002.");
        DocumentStore store=new DocumentStore(storeFile.toString());
        service=new DocumentService(store);
    }
    @AfterAll
    static void cleanup() throws Exception
    {
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(tamperedFile);
        Files.deleteIfExists(storeFile);
    }
    @Test
    @Order(1)
    void hashFile_sha256_returnsCorrectLength() throws Exception
    {
        String hash=DocumentHasher.hashFile(testFile,Algorithm.SHA_256);
        assertNotNull(hash);
        assertEquals(64, hash.length(),"SHA-256 hex should be 64 chars");
    }
    @Test
    @Order(2)
    void hashFile_sha512_returnsCorrectLength() throws Exception
    {
        String hash=DocumentHasher.hashFile(testFile,Algorithm.SHA_512);
        assertNotNull(hash);
        assertEquals(128, hash.length(),"SHA-512 hex should be 128 chars");
    }
    @Test
    @Order(3)
    void hashFile_isDeterministic() throws Exception
    {
        String h1=DocumentHasher.hashFile(testFile,Algorithm.SHA_256);
        String h2=DocumentHasher.hashFile(testFile,Algorithm.SHA_256);
        assertEquals(h1,h2,"Same file must always produce same hash");
    }
    @Test
    @Order(4)
    void hashFile_differentFilesProduceDifferentHashes() throws Exception
    {
        String h1=DocumentHasher.hashFile(testFile,Algorithm.SHA_256);
        String h2=DocumentHasher.hashFile(tamperedFile,Algorithm.SHA_256);
        assertNotEquals(h1,h2,"Different file content must produce different hashes");
    }
    @Test
    @Order(5)
    void verifyFile_trueForOriginal() throws Exception
    {
        String hash=DocumentHasher.hashFile(testFile,Algorithm.SHA_256);
        assertTrue(DocumentHasher.verifyFile(testFile,hash,Algorithm.SHA_256));
    }
    @Test
    @Order(6)
    void verifyFile_falseForTampered() throws Exception
    {
        String hash=DocumentHasher.hashFile(testFile,Algorithm.SHA_256);
        assertFalse(DocumentHasher.verifyFile(tamperedFile,hash,Algorithm.SHA_256),
            "Tampered file should NOT match original hash");
    }
    @Test
    @Order(7)
    void register_createsRecordWithId() throws Exception
    {
        DocumentRecord record=service.register(testFile,"Test Student","student@test.com","Degree Certificate","Test University");
        assertNotNull(record.getId());
        assertFalse(record.getHashSha256().isEmpty());
        assertFalse(record.isRevoked());
        registeredId=record.getId();
    }
    @Test
    @Order(8)
    void verify_validDocumentReturnsValid() throws Exception
    {
        assertNotNull(registeredId,"Registered ID must be set from previous test");
        VerificationResult result=service.verify(testFile,registeredId);
        assertTrue(result.valid);
        assertEquals("VALID", result.status);
    }
    @Test
    @Order(9)
    void verify_tamperedDocumentReturnsTampered() throws Exception
    {
        assertNotNull(registeredId,"registeredId must be set");
        VerificationResult result=service.verify(tamperedFile,registeredId);
        assertFalse(result.valid);
        assertEquals("TAMPERED",result.status);
    }
    @Test
    @Order(10)
    void verify_unknownIdReturnsNotFound() throws Exception
    {
        VerificationResult result=service.verify(testFile,"non-existent-id-0000");
        assertFalse(result.valid);
        assertEquals("NOT_FOUND", result.status);
    }
    @Test
    @Order(11)
    void revoke_marksDocumentRevoked() throws Exception
    {
        assertNotNull(registeredId);
        boolean ok=service.revoke(registeredId);
        assertTrue(ok);
        VerificationResult result=service.verify(testFile,registeredId);
        assertFalse(result.valid);
        assertEquals("REVOKED",result.status);
    }
    @Test
    @Order(12)
    void listAll_returnsAtLeastOneRecord() throws Exception
    {
        assertFalse(service.listAll().isEmpty());
    }
}
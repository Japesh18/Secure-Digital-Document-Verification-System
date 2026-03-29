package com.docverify.service;
import com.docverify.core.DocumentHasher;
import com.docverify.core.DocumentHasher.Algorithm;
import com.docverify.model.DocumentRecord;
import com.docverify.storage.DocumentStore;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
public class DocumentService
{
    private final DocumentStore store;
    public DocumentService(DocumentStore store)
    {
        this.store=store;
    }
    public DocumentRecord register(Path filePath,String ownerName,String ownerEmail,String documentType,String issuedBy)
            throws IOException,NoSuchAlgorithmException
            {
                String sha256=DocumentHasher.hashFile(filePath,Algorithm.SHA_256);
                String sha512=DocumentHasher.hashFile(filePath,Algorithm.SHA_512);
                Optional<DocumentRecord>existing=store.findByHash(sha256);
                if(existing.isPresent())
                {
                    System.out.println("[WARN] Document already registered: "+existing.get().getId());
                    return existing.get();
                }
        DocumentRecord record=new DocumentRecord(ownerName,ownerEmail,documentType,filePath.getFileName().toString(),sha256,
        sha512,issuedBy);
        store.save(record);
        System.out.println("[INFO] Registered document: "+record.getId());
        return record;
            }
    public VerificationResult verify(Path filePath, String documentId)
            throws IOException,NoSuchAlgorithmException
            {
                Optional<DocumentRecord>opt=store.findById(documentId);
                if(opt.isEmpty())
                    {
                        return new VerificationResult(false,"NOT_FOUND","No document with ID:"+documentId,null);
                    }
        DocumentRecord record=opt.get();
        if(record.isRevoked())
            {
                return new VerificationResult(false,"REVOKED","Document has been revoked.",record);
            }
        boolean sha256Match=DocumentHasher.verifyFile(filePath,record.getHashSha256(),Algorithm.SHA_256);
        boolean sha512Match=DocumentHasher.verifyFile(filePath,record.getHashSha512(),Algorithm.SHA_512);
        if(sha256Match && sha512Match)
            {
                return new VerificationResult(true,"VALID","Document is authentic and unmodified.",record);
            }
        else
            {
                return new VerificationResult(false,"TAMPERED","Hash mismatch! Document may have been altered.", record);
            }
            }
    public VerificationResult verifyByHash(String sha256Hash)
            throws IOException
            {
                Optional<DocumentRecord>opt=store.findByHash(sha256Hash);
                if(opt.isEmpty())
                    {
                        return new VerificationResult(false,"NOT_FOUND","No document matches this hash.",null);
                    }
                DocumentRecord record=opt.get();
                if(record.isRevoked())
                    {
                        return new VerificationResult(false,"REVOKED","Document has been revoked.",record);
                    }
                return new VerificationResult(true,"VALID","Document hash found and is authentic.",record);
            }
    public boolean revoke(String documentId) throws IOException
    {
        return store.revokeById(documentId);
    }
    public List<DocumentRecord> listAll() throws IOException
    {
        return store.loadAll();
    }
    public static class VerificationResult
    {
        public final boolean valid;
        public final String status,message;
        public final DocumentRecord record;
        public VerificationResult(boolean valid,String status,String message,DocumentRecord record)
        {
            this.valid=valid;
            this.status=status;
            this.message=message;
            this.record=record;
        }
        @Override
        public String toString()
        {
            return String.format("[%s] %s%s",status,message,record!=null?"\n  → "+record:"");
        }
    }
}
package com.docverify.model;
import java.time.LocalDateTime;
import java.util.UUID;
public class DocumentRecord
{
    private String id,ownerName,ownerEmail,documentType,originalFileName,hashSha256,hashSha512,issuedBy;
    private LocalDateTime issuedAt;
    private boolean revoked;
    public DocumentRecord(String ownerName,String ownerEmail,String documentType,String originalFileName,String hashSha256,
        String hashSha512,String issuedBy)
        {
        this.id=UUID.randomUUID().toString();
        this.ownerName=ownerName;
        this.ownerEmail=ownerEmail;
        this.documentType=documentType;
        this.originalFileName=originalFileName;
        this.hashSha256=hashSha256;
        this.hashSha512=hashSha512;
        this.issuedAt=LocalDateTime.now();
        this.issuedBy=issuedBy;
        this.revoked=false;
    }
    public DocumentRecord(String id,String ownerName,String ownerEmail,String documentType,String originalFileName,
        String hashSha256,String hashSha512,LocalDateTime issuedAt,String issuedBy,boolean revoked)
        {
        this.id=id;
        this.ownerName=ownerName;
        this.ownerEmail=ownerEmail;
        this.documentType=documentType;
        this.originalFileName=originalFileName;
        this.hashSha256=hashSha256;
        this.hashSha512=hashSha512;
        this.issuedAt=issuedAt;
        this.issuedBy=issuedBy;
        this.revoked=revoked;
    }
    public String getId()
    {
        return id;
    }
    public String getOwnerName()
    {
        return ownerName;
    }
    public String getOwnerEmail()
    {
        return ownerEmail;
    }
    public String getDocumentType()
    {
        return documentType;
    }
    public String getOriginalFileName()
    {
        return originalFileName;
    }
    public String getHashSha256()
    {
        return hashSha256;
    }
    public String getHashSha512()
    {
        return hashSha512;
    }
    public LocalDateTime getIssuedAt()
    {
        return issuedAt;
    }
    public String getIssuedBy()
    {
        return issuedBy;
    }
    public boolean isRevoked()
    {
        return revoked;
    }
    public void revoke()
    {
        this.revoked=true;
    }
    @Override
    public String toString()
    {
        return String.format("[%s] %s | Owner: %s | Type: %s | Revoked: %s | Issued: %s",id,originalFileName,ownerName,
        documentType,revoked,issuedAt);
    }
}
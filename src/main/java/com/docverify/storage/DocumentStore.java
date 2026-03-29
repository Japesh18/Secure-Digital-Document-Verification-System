package com.docverify.storage;
import com.docverify.model.DocumentRecord;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
public class DocumentStore
{
    private final Path storePath;
    private static final String HEADER="id,ownerName,ownerEmail,documentType,originalFileName,hashSha256,hashSha512,issuedAt,issuedBy,revoked";
    public DocumentStore(String storeFilePath) throws IOException
    {
        this.storePath=Paths.get(storeFilePath);
        if(!Files.exists(storePath))
            {
                Path parent=storePath.getParent();
                if (parent!=null)
                    {
                        Files.createDirectories(parent);
                    }
                Files.writeString(storePath,HEADER+System.lineSeparator());
            }
    }
    public synchronized void save(DocumentRecord record) throws IOException
    {
        String line=String.join(",",record.getId(),escape(record.getOwnerName()),escape(record.getOwnerEmail()),
            escape(record.getDocumentType()),escape(record.getOriginalFileName()),record.getHashSha256(),
            record.getHashSha512(),record.getIssuedAt().toString(),escape(record.getIssuedBy()),
            String.valueOf(record.isRevoked()));
        Files.writeString(storePath, line + System.lineSeparator(), StandardOpenOption.APPEND);
    }
    public List<DocumentRecord>loadAll() throws IOException
    {
        return Files.lines(storePath)
            .skip(1)
            .filter(l->!l.isBlank())
            .map(this::parseLine)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    public Optional<DocumentRecord>findByHash(String hash) throws IOException
    {
        return loadAll().stream()
            .filter(r->r.getHashSha256().equalsIgnoreCase(hash))
            .findFirst();
    }
    public Optional<DocumentRecord>findById(String id) throws IOException
    {
        return loadAll().stream()
            .filter(r->r.getId().equals(id))
            .findFirst();
    }
    public synchronized boolean revokeById(String id) throws IOException
    {
        List<DocumentRecord>all=loadAll();
        boolean found=false;
        for(DocumentRecord r:all)
            {
                if(r.getId().equals(id))
                    {
                        r.revoke();
                        found=true;
                    }
            }
        if(found)
            {
                rewriteAll(all);
            }
        return found;
    }
    private void rewriteAll(List<DocumentRecord>records) throws IOException
    {
        StringBuilder sb=new StringBuilder(HEADER).append(System.lineSeparator());
        for (DocumentRecord r:records)
            {
                sb.append(String.join(",",r.getId(),escape(r.getOwnerName()),escape(r.getOwnerEmail()),
                escape(r.getDocumentType()),escape(r.getOriginalFileName()),r.getHashSha256(),
                r.getHashSha512(),r.getIssuedAt().toString(),escape(r.getIssuedBy()),String.valueOf(r.isRevoked())
            )).append(System.lineSeparator());
        }
        Files.writeString(storePath,sb.toString(),StandardOpenOption.TRUNCATE_EXISTING);
    }
    private DocumentRecord parseLine(String line)
    {
        try {
            String[] parts = line.split(",", 10);
            return new DocumentRecord(parts[0],parts[1],parts[2],parts[3],parts[4],parts[5],parts[6],
                LocalDateTime.parse(parts[7]),parts[8], Boolean.parseBoolean(parts[9])
            );
        } catch (Exception e)
        {
            return null;
        }
    }
    private String escape(String value)
    {
        return value==null?"":value.replace(",",";");
    }
}
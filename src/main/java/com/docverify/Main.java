package com.docverify;
import com.docverify.model.DocumentRecord;
import com.docverify.service.DocumentService;
import com.docverify.service.DocumentService.VerificationResult;
import com.docverify.storage.DocumentStore;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
public class Main
{
    public static void main(String[] args)
    {
        System.out.println("""
            ╔══════════════════════════════════════════════╗
            ║   Secure Digital Document Verification      ║
            ║   Cryptographic Integrity via SHA-256/512   ║
            ╚══════════════════════════════════════════════╝
            """);
        try
        {
            DocumentStore store=new DocumentStore("data/documents.csv");
            DocumentService service=new DocumentService(store);
            Scanner sc=new Scanner(System.in);
            while(true)
                {
                    printMenu();
                    String choice=sc.nextLine().trim();
                    switch (choice)
                    {
                        case "1"->registerDocument(sc,service);
                        case "2"->verifyByIdAndFile(sc,service);
                        case "3"->verifyByHash(sc,service);
                        case "4"->revokeDocument(sc,service);
                        case "5"->listAll(service);
                        case "0"->{
                            System.out.println("Goodbye.");
                            return;
                        }
                        default->System.out.println("[!] Invalid option.");
                    }
                System.out.println();
                }
        }
        catch(Exception e)
        {
            System.err.println("[ERROR] Fatal: "+e.getMessage());
            e.printStackTrace();
        }
    }
    private static void registerDocument(Scanner sc,DocumentService service)
    {
        try
        {
            System.out.print("  File path: ");
            String filePath=sc.nextLine().trim();
            System.out.print("  Owner name: ");
            String owner=sc.nextLine().trim();
            System.out.print("  Owner email: ");
            String email=sc.nextLine().trim();
            System.out.print("  Document type: ");
            String docType=sc.nextLine().trim();
            System.out.print("  Issued by (authority): ");
            String issuedBy=sc.nextLine().trim();
            DocumentRecord record=service.register(Paths.get(filePath),owner,email,docType,issuedBy);
            System.out.println("\n  ✅ Document registered successfully!");
            System.out.println("  ID:         "+record.getId());
            System.out.println("  SHA-256:    "+record.getHashSha256());
            System.out.println("  SHA-512:    "+record.getHashSha512().substring(0, 32)+"...");
            System.out.println("  Issued At:  "+record.getIssuedAt());
        }
        catch (Exception e)
        {
            System.out.println("  ❌ Error: "+e.getMessage());
        }
    }
    private static void verifyByIdAndFile(Scanner sc,DocumentService service)
    {
        try
        {
            System.out.print("  Document ID: ");
            String id=sc.nextLine().trim();
            System.out.print("  File path:   ");
            String filePath=sc.nextLine().trim();
            VerificationResult result=service.verify(Paths.get(filePath),id);
            printResult(result);
        }
        catch (Exception e)
        {
            System.out.println("  ❌ Error: "+e.getMessage());
        }
    }
    private static void verifyByHash(Scanner sc,DocumentService service)
    {
        try
        {
            System.out.print("  SHA-256 hash: ");
            String hash=sc.nextLine().trim();
            VerificationResult result=service.verifyByHash(hash);
            printResult(result);
        }
        catch (Exception e)
        {
            System.out.println("  ❌ Error: "+e.getMessage());
        }
    }
    private static void revokeDocument(Scanner sc,DocumentService service)
    {
        try
        {
            System.out.print("  Document ID to revoke: ");
            String id=sc.nextLine().trim();
            boolean ok=service.revoke(id);
            System.out.println(ok?"  ✅ Document revoked.":"  ❌ Document ID not found.");
        }
        catch (Exception e)
        {
            System.out.println("  ❌ Error: "+e.getMessage());
        }
    }
    private static void listAll(DocumentService service)
    {
        try
        {
            List<DocumentRecord>all=service.listAll();
            if(all.isEmpty())
                {
                    System.out.println("  No documents registered yet.");
                    return;
                }
            System.out.printf("  %-36s %-20s %-25s %-10s%n","ID","Owner","Type","Status");
            System.out.println("  "+"─".repeat(95));
            for(DocumentRecord r:all)
                {
                    System.out.printf("  %-36s %-20s %-25s %-10s%n",r.getId(),r.getOwnerName(),r.getDocumentType(),r.isRevoked()?"REVOKED":"VALID");
                }
        }
        catch (Exception e)
        {
            System.out.println("  ❌ Error: "+e.getMessage());
        }
    }
    private static void printMenu()
    {
        System.out.println("""
            ┌─ Commands ──────────────────────────┐
            │  1. Register document               │
            │  2. Verify document (ID + file)     │
            │  3. Verify document (by hash)       │
            │  4. Revoke document                 │
            │  5. List all documents              │
            │  0. Exit                            │
            └─────────────────────────────────────┘
            Choice: """);
    }
    private static void printResult(VerificationResult result)
    {
        String icon=result.valid?"✅":"❌";
        System.out.println("\n  "+icon+" Status: "+result.status);
        System.out.println("  "+result.message);
        if(result.record!=null)
            {
                System.out.println("  Owner:     "+result.record.getOwnerName());
                System.out.println("  Type:      "+result.record.getDocumentType());
                System.out.println("  Issued By: "+result.record.getIssuedBy());
                System.out.println("  Issued At: "+result.record.getIssuedAt());
            }
    }
}
package com.azureblob;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import reactor.core.publisher.Flux;

@Component
public class AzureBlobService {

    @Autowired
    BlobServiceClient blobServiceClient;

    @Autowired
    BlobContainerClient blobContainerClient;

    public String upload(MultipartFile multipartFile)
            throws IOException {

        // Todo UUID
        BlobClient blob = blobContainerClient
                .getBlobClient(multipartFile.getOriginalFilename());

        if(blobexists(blob))
        {
            blob.delete();
        }

//        BlobAsyncClient blobAsyncClient = null;
//        blobAsyncClient.uploadFromFile(multipartFile.toString(), true)
//                .doOnError(throwable -> System.err.printf("Failed to upload from file %s%n", throwable.getMessage()))
//                .doOnSuccess(success -> System.out.printf("Upload file succeeded"))
//                .subscribe();
        blob.upload(multipartFile.getInputStream(),
                multipartFile.getSize(), true);

        return multipartFile.getOriginalFilename();
    }


    public Boolean copyBlob(String fileName, String sourceContainer, String destinationContainer){
        System.out.println("hi i M GOOD");


//        String fileName = "";
//        for (BlobItem blobItem : blobServiceClient.getBlobContainerClient(sourceContainer).listBlobs()) {
//            fileName=blobItem.getName();
//        }
//        System.out.println(fileName);

        BlobClient sourceBlob=blobServiceClient.getBlobContainerClient(sourceContainer).getBlobClient(fileName);
        if(sourceBlob.exists()){
            BlobClient destBlob = blobServiceClient.getBlobContainerClient(destinationContainer).getBlobClient(fileName);
            BlobLeaseClient lease = new BlobLeaseClientBuilder().blobClient(sourceBlob).buildClient();
            lease.acquireLease(-1);
            BlobProperties sourceBlobProps=sourceBlob.getProperties();
            System.out.printf("Source blob lease state: %s%n", sourceBlobProps.getLeaseState().toString());
            destBlob.copyFromUrl(sourceBlob.getBlobUrl());


            // Get the destination blob properties
            BlobProperties destBlobProps = destBlob.getProperties();
            System.out.printf("Copy status: %s%n", destBlobProps.getCopyStatus());
            System.out.printf("Copy progress: %s%n", destBlobProps.getCopyProgress());
            System.out.printf("Copy completion time: %s%n", destBlobProps.getCopyCompletionTime());
            System.out.printf("Total bytes copied: %d%n", destBlobProps.getBlobSize());

            // Break the lease on the source blob
            if(sourceBlobProps.getLeaseState()== LeaseStateType.LEASED){
                lease.breakLease();
                sourceBlobProps=sourceBlob.getProperties();
                System.out.printf("Source blob lease state: %s%n", sourceBlobProps.getLeaseState().toString());
            }
            else{
                System.out.println("Source Blob does not exists");
            }
            return true;
        }
        return false;
    }

    public Boolean blobexists(BlobClient blob){

        if(blob.exists()){
            //System.out.println("Hello");
            return true;
        }
        else{
            return false;
        }
    }

    public byte[] getFile(String fileName)
            throws URISyntaxException {

        BlobClient blob = blobContainerClient.getBlobClient(fileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blob.download(outputStream);
        final byte[] bytes = outputStream.toByteArray();
        return bytes;

    }

    public List<String> listBlobs() {

        PagedIterable<BlobItem> items = blobContainerClient.listBlobs();
        List<String> names = new ArrayList<String>();
        for (BlobItem item : items) {
            names.add(item.getName());
        }
        return names;
    }

    public Boolean deleteBlob(String blobName) {

        BlobClient blob = blobContainerClient.getBlobClient(blobName);
        blob.delete();
        return true;
    }

}
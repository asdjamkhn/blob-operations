package JsonStreams;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.specialized.BlobLeaseClient;
import com.azure.storage.blob.specialized.BlobLeaseClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JsonStreams {

    public static void main(String[] args) throws IOException {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString("DefaultEndpointsProtocol=https;AccountName=shanimanistorage;AccountKey=mlwM2ufbtNTKGZBtNRfPq8KnWr3xcE00BRMPr/XfebB1cN1hdvobeAGV1Do8r4MQRE5oV4niNb8Y+AStyLxxbQ==;EndpointSuffix=core.windows.net").buildClient();
        // Get the container client for the source container
        BlobContainerClient sourceContainer = blobServiceClient.getBlobContainerClient("quickstartblobs5");
        // Get the container client for the destination container
        BlobContainerClient destinationContainer = blobServiceClient.getBlobContainerClient("quickstartblobs6");
        // Get the list of blobs in the source container
        // Iterate through all the blobs in the source container

        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        for (BlobItem blob : sourceContainer.listBlobs()) {

            threadPool.execute(()->{
                BlobLeaseClient blobLeaseClient=new BlobLeaseClientBuilder().containerClient(sourceContainer).buildClient();


                // Retrieve the blob as a stream
                InputStream blobStream = sourceContainer.getBlobClient(blob.getName()).openInputStream();

                //Acquire lease for 15-60seconds
                blobLeaseClient.acquireLease(60);

                //Upload the blob stream to the destination container
                destinationContainer.getBlobClient(blob.getName()).upload(BinaryData.fromStream(blobStream), true);
                // Release the lease for the source blob
                blobLeaseClient.releaseLease();

            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        //Shut down the thread Pool
        threadPool.shutdown();
    }
}

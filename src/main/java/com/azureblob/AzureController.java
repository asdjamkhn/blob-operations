package com.azureblob;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class AzureController {

    @Autowired
    private AzureBlobService azureBlobAdapter;

    @PostMapping
    public ResponseEntity<String> upload
            (@RequestParam ("files") MultipartFile file)
            throws IOException {

        String fileName = azureBlobAdapter.upload(file);
        return ResponseEntity.ok(fileName);
    }

    @PutMapping()
    public ResponseEntity<Boolean>copy (@RequestParam("fileName") String fileName,
                                        @RequestParam("sourceContainer")String sourceContainer,
                                        @RequestParam("destinationContainer")String destinationContainer){
        azureBlobAdapter.copyBlob(fileName, sourceContainer,destinationContainer);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<String>> getAllBlobs() {

        List<String> items = azureBlobAdapter.listBlobs();
        System.out.println(items);
        return ResponseEntity.ok(items);

    }

    @DeleteMapping
    public ResponseEntity<Boolean> delete
            (@RequestParam String fileName) {

        azureBlobAdapter.deleteBlob(fileName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> getFile
            (@RequestParam String fileName)
            throws URISyntaxException {

        ByteArrayResource resource =
                new ByteArrayResource(azureBlobAdapter
                        .getFile(fileName));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\""
                        + fileName + "\"");

        return ResponseEntity.ok().contentType(MediaType
                        .APPLICATION_OCTET_STREAM)
                .headers(headers).body(resource);
    }
}
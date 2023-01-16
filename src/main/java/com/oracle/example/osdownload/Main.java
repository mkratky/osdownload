package com.oracle.example.osdownload;

import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.requests.GetBucketRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.responses.GetBucketResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.ListObjectsResponse;

import java.util.Iterator;
import java.util.List;

public class Main {

    private ObjectStorage objectStorageClient;
    private ResourcePrincipalAuthenticationDetailsProvider provider;
    private static String bucketName;


    public static void main(String[] args) {
        bucketName = args[0];

        Main app = new Main();
        app.initOciClients();
        app.downloadAll();
    }

    private void initOciClients() {
        System.out.println("Inside initOciClients");
        try {
            provider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
            System.err.println("ResourcePrincipalAuthenticationDetailsProvider setup");
            objectStorageClient = ObjectStorageClient.builder().build(provider);
            System.out.println("ObjectStorage client setup");
        } catch (Exception ex) {
            System.out.println(new StringBuilder().append("Could not initialize the ObjectStorage service ").append(ex.getMessage()).toString());
            ex.printStackTrace();
            throw new RuntimeException("failed to init oci client", ex);
        }
    }

    private void downloadAll(){
        try {
            String namespace = objectStorageClient
                    .getNamespace(GetNamespaceRequest.builder().build())
                    .getValue();
            System.out.println("Using namespace: " + namespace);
            GetBucketRequest request =
                    GetBucketRequest.builder()
                            .namespaceName(namespace)
                            .bucketName(bucketName)
                            .build();
            GetBucketResponse response = objectStorageClient.getBucket(request);
            System.out.println(bucketName +" bucket exists");

            ListObjectsRequest listRequest =
                    ListObjectsRequest.builder()
                            .namespaceName(namespace)
                            .bucketName(bucketName)
                            .build();
            ListObjectsResponse listResponse = objectStorageClient.listObjects(listRequest);
            List<ObjectSummary> objectList = listResponse.getListObjects().getObjects();
            Iterator<ObjectSummary> objectIterator = objectList.listIterator();
            while (objectIterator.hasNext()){
                String objectName = objectIterator.next().getName();
                System.out.println(objectName);
            }

        }catch (BmcException bmcException) {
            System.out.println("Can't find the existing Bucket Name : " + bucketName);
            throw bmcException;
        }
    }

}
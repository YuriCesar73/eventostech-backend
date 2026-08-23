package com.eventostec.api.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

@Service
public class StorageService {
	@Autowired
	private MinioClient minioClient;

	public void uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) {
		try {
			boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
			if (!found) {
				minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
			}
			minioClient.putObject(
					PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
							inputStream, (long) inputStream.available(), (long) -1)
					.contentType(contentType)
					.build());
		} catch (Exception e) {
			throw new  RuntimeException("Error occurred: " + e.getMessage());
		}
	}
	
	public InputStream getFile(String bucketName, String objectName) {
		try {
			InputStream stream = minioClient.getObject(GetObjectArgs
					.builder()
					.bucket(bucketName)
					.object(objectName)
					.build());

			return stream;
			
		} catch (MinioException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}

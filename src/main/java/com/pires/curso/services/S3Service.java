package com.pires.curso.services;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.pires.curso.config.S3Config;

@Service
public class S3Service {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private S3Config config;

	public void uploadFile(String localFilePath) {
		try {
			File file = new File(localFilePath);
			logger.info("Iniciando o upload");
			config.s3client().putObject(new PutObjectRequest(config.getBucketName(), "teste", file));
			logger.info("Upload finalizado");
		} catch (AmazonServiceException e) {
			logger.info("AmazonServiceException " + e.getMessage());
			logger.info("AmazonServiceException " + e.getErrorCode());
		} catch (AmazonClientException e) {
			logger.info("AmazonClientException " + e.getMessage());
		}
	}

}

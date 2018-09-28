package com.pires.curso.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.pires.curso.config.S3Config;
import com.pires.curso.services.exceptions.FileException;

@Service
public class S3Service {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private S3Config config;

	public URI uploadFile(MultipartFile multipartFile) {

		try {
			String fileName = multipartFile.getOriginalFilename();
			InputStream is = multipartFile.getInputStream();
			String contentType = multipartFile.getContentType();
			return uploadFile(is, fileName, contentType);
		} catch (IOException e) {
			throw new FileException("Erro de IO " + e.getMessage());
		}

	}

	public URI uploadFile(InputStream is, String fileName, String contentType) {
		try {
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentType(contentType);

			logger.info("Iniciando o upload");
			config.s3client().putObject(config.getBucketName(), fileName, is, meta);
			logger.info("Upload finalizado");

			return config.s3client().getUrl(config.getBucketName(), fileName).toURI();
		} catch (URISyntaxException e) {
			throw new FileException("Erro ao convrter Url para Uri");
		}

	}

}

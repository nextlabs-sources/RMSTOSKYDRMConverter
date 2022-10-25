package com.nextlabs.edrm.converter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EDRMConverter {
	static Logger logger = Logger.getLogger(EDRMConverter.class);

	public static void main(String args[]) throws URISyntaxException, UnsupportedEncodingException {
		 
		Security.addProvider(new BouncyCastleProvider());

		String parentFolder = args[0];

		URL jarUrl =EDRMConverter.class.getResource("/com/nextlabs/edrm/converter/EDRMConverter.class");
		String jarPath=URLDecoder.decode(jarUrl.getPath(),"UTF-8");
		jarPath=jarPath.substring(6, jarPath.indexOf("EDRMConverter.jar"));
		System.out.println("path: "+jarPath);
		System.setProperty("path", jarPath);
		 String log4jConfigFile =jarPath+
		 "conf" + File.separator + "log4j.xml";
		  DOMConfigurator.configure(log4jConfigFile);
		  System.out.println(log4jConfigFile);
		 
		  FileQueue.initialize(); List<String> allFolderContents =
		  getAllFolderContents(parentFolder); findOldNxlFiles(allFolderContents);
		  
		  logger.info(FileQueue.oldNXLFileList.size());
		  OldEDRMConverter.initializeRightsManager(jarPath);
		  FileQueue.oldNXLFileList.parallelStream().forEach(OldEDRMConverter::
		  decryptFile); logger.info(FileQueue.plainFileList.size()); Long startTime =
		  System.currentTimeMillis(); SKYDRMConverter.initializeRightsManager(jarPath);
		  FileQueue.plainFileList.parallelStream().forEach(SKYDRMConverter::encryptFile
		  );
		  
		  Long currentTime = System.currentTimeMillis(); Long totalTime = (currentTime
		 - startTime); logger.info("Total time in seconds:" + totalTime + " ms");
		  
		 System.exit(0);
		
	}

	private static void findOldNxlFiles(List<String> fileList) {
		List<String> fileLists = fileList.stream().filter(file -> file.endsWith(".nxl")).collect(Collectors.toList());
		for (String fileName : fileLists) {
			File file = new File(fileName);
			FileQueue.oldNXLFileList.add(file);
		}

	}

	private static List<String> getAllFolderContents(String path) {
		List<String> collect = null;
		Path start = Paths.get(path);
		try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
			collect = stream.map(String::valueOf).sorted().collect(Collectors.toList());

		} catch (IOException e) {
//			logger.error(e.getMessage(), e);

		}
		return collect;
	}

}

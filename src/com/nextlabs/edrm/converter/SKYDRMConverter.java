package com.nextlabs.edrm.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.bluejungle.framework.crypt.IDecryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.common.shared.Constants.TokenGroupType;
import com.nextlabs.nxl.NxlException;
import com.nextlabs.nxl.RightsManager;

public class SKYDRMConverter {
	static Logger logger = Logger.getLogger(SKYDRMConverter.class);
	static RightsManager rightsManager;
	static Map<String, String[]> tagMap;
	static 	String tenantName;
	public static void initializeRightsManager(String jarPath) {
		String routerUrl;
		int appID;
		String appKey;
		IDecryptor decryptor = new ReversibleEncryptor();
		Properties pcProperties = new Properties();
		String pcPropertiesPath = jarPath+ "conf" + File.separator
				+ "ConversionSDK.properties";
		
		File pcPropertiesFile = new File(pcPropertiesPath);
		
		if (pcPropertiesFile.exists()) {

			try {
				FileInputStream fis = new FileInputStream(pcPropertiesFile);
				pcProperties.load(fis);
				routerUrl=pcProperties.getProperty("Skydrm_router_url");
				appID=Integer.parseInt(pcProperties.getProperty("Skydrm_app_id"));
				appKey=decryptor.decrypt(pcProperties.getProperty("Skydrm_app_key"));
				tenantName=pcProperties.getProperty("SKYDRM_tenant_name");
				String key=pcProperties.getProperty("Skydrm_classify_key");
				String value=pcProperties.getProperty("Skydrm_classify_value");
				String values[]= new String[1];
				values[0]=value;
				tagMap=new HashMap<String, String[]>();
				tagMap.put(key, values);
				
				rightsManager = new RightsManager(routerUrl, appID, appKey);
			} catch (IOException | NxlException e1) {
				logger.warn("Failed to load properties due to error message \"" + e1.getMessage()
						+ "\". Process Ends now without Decrypting files");
				System.exit(0);
			}
		} else {
			logger.warn("ConversionSDK.properties is missing.Check the file is located in the path  " + pcPropertiesPath
					+ ". Process Ends now without encrypting using new nxl files");
			System.exit(0);
		}
	}
	public static void encryptFile(String inputFile) {
		String outputFile=inputFile+".nxl";
		try {
			
			rightsManager.encrypt(inputFile, outputFile, null, null, tagMap, tenantName,
					TokenGroupType.TOKENGROUP_SYSTEMBUCKET);
			logger.info("SAPEDRM: Encryption completed for "+inputFile);
			new File(inputFile).delete();
		} catch (NxlException e) {

			logger.info("SAPEDRM: Error while performing Encryption");
			logger.error(e);
			
		}


	}
	 
}

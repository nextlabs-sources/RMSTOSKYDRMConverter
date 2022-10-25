package com.nextlabs.edrm.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.bluejungle.framework.crypt.IDecryptor;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.nxl.crypt.RightsManager;
import com.nextlabs.nxl.exception.NXRTERROR;
import com.nextlabs.nxl.pojos.PolicyControllerDetails;

public class OldEDRMConverter {
	static Logger logger = Logger.getLogger(OldEDRMConverter.class);
	static RightsManager manager;


	public static void initializeRightsManager(String jarPath) {
		PolicyControllerDetails policyControllerDetails = new PolicyControllerDetails();

		Properties pcProperties = new Properties();
		String pcPropertiesPath = jarPath + "conf" + File.separator
				+ "ConversionSDK.properties";
		String keyStorePath=jarPath + "conf" + File.separator
				+ "rmskmc-keystore.jks";
		String trustStorePath=jarPath + "conf" + File.separator
				+ "rmskmc-truststore.jks";
		File pcPropertiesFile = new File(pcPropertiesPath);
		IDecryptor decryptor = new ReversibleEncryptor();
		if (pcPropertiesFile.exists()) {

			try {
				FileInputStream fis = new FileInputStream(pcPropertiesFile);
				pcProperties.load(fis);
				policyControllerDetails.setKeyStoreName(keyStorePath);
				policyControllerDetails
						.setKeyStorePassword(decryptor.decrypt(pcProperties.getProperty("KeyStorePassword")));
				policyControllerDetails.setPcHostName(pcProperties.getProperty("PolicyControllerHostName"));
				policyControllerDetails.setTrustStoreName(trustStorePath);
				policyControllerDetails
						.setRmiPortNum(Integer.parseInt(pcProperties.getProperty("PolicyControllerKMCRMIport")));
				policyControllerDetails
						.setTrustStorePasswd(decryptor.decrypt(pcProperties.getProperty("TrustStorePassword")));
				manager = new RightsManager(policyControllerDetails);
				
			} catch (IOException e1) {
				logger.warn("Failed to load properties due to error message \"" + e1.getMessage()
						+ "\". Process Ends now without Decrypting files");
				System.exit(0);
			}

			catch (NXRTERROR e) {
				logger.warn("Failed to initialize RightsManager due to error message \"" + e.getMessage()
						+ "\". Process Ends now without Decrypting files");
				System.exit(0);
			} catch (Exception e2) {
				logger.warn("Failed to initialize RightsManager due to error message \"" + e2.getMessage()
						+ "\". Process Ends now without Decrypting files");
				System.exit(0);
			}
		} else {
			logger.warn("ConversionSDK.properties is missing.Check the file is located in the path  " + pcPropertiesPath
					+ ". Process Ends now without Decrypting files");
			System.exit(0);
		}
	}

	public static void decryptFile(File file) {
		String plainTextFilePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4);
		if (null != manager) {
			try {
				if (file.getName().endsWith(".nxl")) {
					manager.decrypt(file, plainTextFilePath, null);
					logger.info(file.getName() + " Decrypted Successfully");
					file.delete();
					FileQueue.plainFileList.add(plainTextFilePath);

				} else {
					logger.warn("The file " + file.getName()
							+ " is not a Nextlabs protected file. Continuing to next file in the folder or next conversion process flow.");
				}
			} catch (Exception e) {

				logger.error("Error occured :" + e.getMessage());
				logger.error("Failed to decrypt file :" + file.getName());
			}
		}

	}
}

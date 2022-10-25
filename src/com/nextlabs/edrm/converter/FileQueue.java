package com.nextlabs.edrm.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileQueue {
public static List<File> oldNXLFileList;
public static List<String> plainFileList;
public static void initialize()
{
	oldNXLFileList=new ArrayList<File>();
	plainFileList=new ArrayList<String>();
}


}

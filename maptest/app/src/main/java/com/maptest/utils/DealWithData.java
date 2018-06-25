package com.maptest.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ZMJ on 2018/1/3.
 */
public class DealWithData {
	private  final String ALLBOUNDARY = java.util.UUID.randomUUID().toString();
	private final String _boundary = "----" + ALLBOUNDARY;
	public final String boundary = "--" + _boundary;
	private static final String END = "\r\n";
	String BOUNDARY = _boundary;
	String PREFIX = "--", LINEND = "\r\n";
	public static final String MULTIPART_FROM_DATA = "multipart/form-data";
	String CHARSET = "UTF-8";


	public  void initHttpUrl(URL url)throws IOException{
		HttpURLConnection connection = null;

		connection = (HttpURLConnection)url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");

		connection.setRequestProperty("Charset","UTF-8");
		connection.setRequestProperty("Connection","Keep-Alive");
		connection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary" + boundary );
	}
	/**
	 * 添加数据
	 * @param dos
	 * @param key
	 * @param value
	 * @param chinese
	 * @throws IOException
	 */
	public  void addParams(DataOutputStream dos,final String key,final String value,Boolean chinese)throws IOException{
		dos.writeBytes(boundary);
		dos.writeBytes(END);
		dos.writeBytes("Content-Disposition:form-data;name=\"" + key + "\"");
		dos.writeBytes(END);
		dos.writeBytes(END);
		if(chinese){
			dos.write(value.getBytes());
		}else {
			dos.writeBytes(value);
		}
		dos.writeBytes(END);
	}

	/**
	 * 添加图片
	 * @param dos
	 * @param fileName
	 * @param index
	 * @throws IOException
	 */
	public void addFile(DataOutputStream dos,final String fileName,final int index)throws IOException{

	}

}

package me.kuku.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtils {
	public static byte[] gzip(byte[] content) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		GZIPOutputStream gos=new GZIPOutputStream(bao);

		ByteArrayInputStream bai =new ByteArrayInputStream(content);
		byte[ ] buffer=new byte[1024];
		int n;
		while((n = bai.read(buffer)) != -1){
			gos.write(buffer, 0, n);
		}
		gos.flush();
		gos.close();
		return bao.toByteArray();
	}

	public static byte[] unGzip(byte[] content) throws IOException{
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(content));
		byte[] buffer = new byte[1024];
		int n;
		while((n = gis.read(buffer)) != -1){
			bao.write(buffer, 0, n);
		}

		return bao.toByteArray();
	}
}

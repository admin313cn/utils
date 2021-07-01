package me.kuku.utils;

import java.io.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class IOUtils {

	private static final File tmpLocation = new File("tmp");
	private static final String tmp = "tmp";

	public static File writeTmpFile(String fileName, InputStream is, boolean isClose){
		if (!tmpLocation.exists())
			tmpLocation.mkdir();
		String path = tmp + File.separator + fileName;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			write(is, fos, isClose);
		}catch (IOException e){
			e.printStackTrace();
		}
		return new File(path);
	}

	public static File writeTmpFile(String fileName, InputStream is){
		return writeTmpFile(fileName, is, true);
	}

	public static File writeTmpFile(String fileName, byte[] bytes){
		if (!tmpLocation.exists())
			tmpLocation.mkdir();
		String path = tmp + File.separator + fileName;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(path);
			fos.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fos);
		}
		return new File(path);
	}

	public static void close(Closeable closeable){
		if (closeable != null){
			try {
				closeable.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] read(File file){
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static byte[] read(InputStream fis, boolean isClose){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		write(fis, bos, isClose);
		return bos.toByteArray();
	}

	public static void write(InputStream is, OutputStream os, boolean isClose){
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = is.read(buffer)) != -1){
				os.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isClose) {
				close(is);
			}
			close(os);
		}
	}

	public static void write(InputStream is, OutputStream os){
		write(is, os, true);
	}

	public static byte[] read(InputStream fis){
		return read(fis, true);
	}


}

package com.gobi.thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileUtils {

	public static void main(String[] args) throws IOException {
		touch("/100/EXT_100_SUCCESS");
	}

	public static void touch(final File file) throws IOException {
		if (!file.exists()) {
			openOutputStream(file).close();
		}
		final boolean success = file.setLastModified(System.currentTimeMillis());
		if (!success) {
			throw new IOException("Unable to set the last modification time for " + file);
		}
	}

	public static FileOutputStream openOutputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			final File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}
		return new FileOutputStream(file);
	}

	public static void touch(final String fileName) {
		try {
			if (fileName != null && !fileName.isEmpty()) {
				touch(getFile(fileName));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteDirectory(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					deleteDirectory(entry);
				}
			}
		}
		Files.delete(path);
	}

	public static boolean deleteQuietly(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return false;
		}

		Path fileToDeletePath = Paths.get(fileName);
		try {
			Files.delete(fileToDeletePath);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static File[] listFiles(final File directory) throws IOException {
		if (!directory.exists()) {
			final String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			final String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		final File[] files = directory.listFiles();
		if (files == null) {  // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}
		return files;
	}

	public static File getFile(final String... names) {
		if (names == null) {
			throw new NullPointerException("names must not be null");
		}
		File file = null;
		for (final String name : names) {
			if (file == null) {
				file = new File(name);
			} else {
				file = new File(file, name);
			}
		}
		return file;
	}

	public static void createDirectories(String folderPath) {

		if (folderPath != null && !folderPath.isEmpty()) {
			Path path = Paths.get(folderPath);

			if (Files.exists(path)) {
				try {
					deleteDirectory(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void createDirectoriesInTempPath(String tempFolderPath) {
		String tempLocation = System.getProperty("java.io.tmpdir");

		if (tempFolderPath != null && !tempFolderPath.isEmpty()) {
			tempLocation = tempLocation + "/" + tempFolderPath;
			createDirectories(tempLocation);
		}
	}

	public static Map<String, String> getListOfCompletedMapping(final String completedMappingPath) {
		String tempLocation = System.getProperty("java.io.tmpdir");
		Map<String, String> completedMappingMap = new HashMap<>();
		File[] fileList = null;

		try {

			if (completedMappingPath != null && !completedMappingPath.isEmpty()) {
				fileList = listFiles(getFile(tempLocation + "/" +completedMappingPath));

				if (fileList != null) {
					for (final File file : fileList) {
						String value = file.getName();
						getCompletedMappingKey(value, completedMappingMap);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return completedMappingMap;
	}

	private static void getCompletedMappingKey(String value, Map<String, String> completedMappingMap) {
		String completedKey = "";
		String completedValue = "";
		String[] valueArray = null;

		if(value != null && !value.isEmpty()) {
			valueArray = value.split("_");
		}

		if (valueArray != null && valueArray.length > 0) {
			completedKey = valueArray[0] + "_" + valueArray[1];
			completedValue = valueArray[2];
		}

		if (completedKey != null && !completedKey.isEmpty()) {
			completedMappingMap.put(completedKey, completedValue);
		}
	}

	public static String getFilePath(String completedMappingPath, String key, String status) {
		StringBuffer buildPath = new StringBuffer(100);
		String tempLocation = System.getProperty("java.io.tmpdir");

		buildPath.append(tempLocation);
		buildPath.append("/");
		buildPath.append(completedMappingPath);
		buildPath.append("/");
		buildPath.append(key);
		buildPath.append("_");
		buildPath.append(status);

		return buildPath.toString();
	}

}

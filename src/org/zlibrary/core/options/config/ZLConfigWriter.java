package org.zlibrary.core.options.config;

import java.io.*;
import java.util.*;

final class ZLConfigWriter implements ZLWriter {
	private final ZLConfigImpl myConfig = (ZLConfigImpl)ZLConfigInstance.getInstance();
	private final File myDestinationDirectory;

	protected ZLConfigWriter(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		myDestinationDirectory = file;
	}

	private void writeConfigFile(String configFileContent, String filePath) {
		File file = new File(filePath);
		try {
			PrintWriter pw = new PrintWriter(file, "UTF-8");
			try {
				pw.write(configFileContent);
			} finally {
				pw.close();
			}
		} catch (FileNotFoundException fnfException) {
			if (!file.getName().toLowerCase().equals("delta.xml")) {
				System.err.println(fnfException.getMessage());
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println(e.getMessage());
		}
	}

	private void deleteConfigFile(String filePath) {
		File file = new File(filePath);
		file.delete();
	}

	//TODO ���� public � ����� �������
	public void writeDelta() {
		this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ myConfig.getDelta(), myDestinationDirectory + "/delta.xml");
	}

	private String configFilePath(String category) {
		return myDestinationDirectory + "/" + category + ".xml";
	}

	public void write() {
		Set<String> usedCategories = myConfig.applyDelta();
		// ���� - ��� ���������, �������� - ���������� ���������������� �����
		Map<String, StringBuffer> configFilesContent = new LinkedHashMap<String, StringBuffer>();
		StringBuffer sb;
		Map<String, Boolean> currentGroupOpenedIn;

		for (String groupName : myConfig.groupNames()) {

			// ���� - ����� ���������, � ������� �� ��� �����, ��� ��� ��� ����
			// �������� - �������� �� �� ��� ��� � �����
			currentGroupOpenedIn = new HashMap<String, Boolean>();

			ZLGroup group = myConfig.getGroup(groupName);
			for (String optionName : group.optionNames()) {
				ZLOptionInfo option = group.getOption(optionName);
				sb = configFilesContent.get(option.getCategory());

				if (currentGroupOpenedIn.get(option.getCategory()) == null) {
					currentGroupOpenedIn.put(option.getCategory(), false);
				}

				if (sb == null) {
					sb = new StringBuffer();
					configFilesContent.put(option.getCategory(), sb);
				}

				if (!currentGroupOpenedIn.get(option.getCategory())) {
					sb.append("  <group name=\"" + groupName + "\">\n");
					currentGroupOpenedIn.put(option.getCategory(), true);
				}
				sb.append(option);
			}

			for (String category : currentGroupOpenedIn.keySet()) {
				configFilesContent.get(category).append("  </group>\n");
			}
		}

		for (String category : usedCategories) {
			this.writeConfigFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + configFilesContent.get(category)
					+ "</config>", configFilePath(category));
		}

		/**
		 * ���� � ����������, ������� �� ��������, ���������� ��, � �������
		 * ����� ��������� ������ �� �����, �� �� ������� ��������������� �����
		 */
		for (String category : usedCategories) {
			if (!configFilesContent.keySet().contains(category)) {
				deleteConfigFile(configFilePath(category));
			}
		}
	}
}

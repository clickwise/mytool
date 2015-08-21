package org.click.lib.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.click.lib.sort.SortStrArray;
import org.click.lib.string.SSO;

public class Format {

	public static int ngramLimit = 7;

	// start from 1
	HashMap<String, Integer> wordDict = new HashMap<String, Integer>();

	// start from 1
	HashMap<String, Integer> labelDict = new HashMap<String, Integer>();

	public List<WORD> processLine(String words) {
		
		String[] tokens = words.split(";");

		String token = "", key = "", value = "";

		if (tokens == null) {
			return null;
		}

		List<WORD> wordList = new ArrayList<WORD>();
		WORD word = null;

		for (int j = 0; j < tokens.length; j++) {

			token = tokens[j];
			if (SSO.tioe(token) || token.trim().equals("-")) {
				continue;
			}

			if (token.split(":").length != 2) {
				continue;
			}

			key = token.split(":")[0].trim();
			value = token.split(":")[1].trim();
			word = new WORD(key, Double.parseDouble(value));
			wordList.add(word);
		}

		return wordList;
	}

	/**
	 * build dicts train file,including word dict and label dict
	 * 
	 * @param preprocess
	 * @param word_dict
	 * @param label_dict
	 */
	public void buildDict(String preprocess, String word_dict, String label_dict) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(preprocess));

			String label = "", words = "", line = "";
			String[] fields = null;
			List<WORD> wordList = null;
			WORD word = null;

			while ((line = br.readLine()) != null) {

				if (SSO.tioe(line)) {
					continue;
				}

				fields = line.split("\001");

				if (fields.length != 2) {
					continue;
				}

				label = fields[0].trim();
				words = fields[1];

				if (!(labelDict.containsKey(label))) {
					labelDict.put(label, labelDict.size() + 1);
				}

				wordList = processLine(words);

				for (int s = 0; s < wordList.size(); s++) {

					word = wordList.get(s);

					if (!(wordDict.containsKey(word.key))) {
						wordDict.put(word.key, wordDict.size() + 1);
					}
				}
			}

			br.close();

			PrintWriter ldpw = new PrintWriter(new FileWriter(label_dict));
			PrintWriter wdpw = new PrintWriter(new FileWriter(word_dict));

			for (Map.Entry<String, Integer> entry : labelDict.entrySet()) {
				ldpw.println(entry.getKey() + "\001" + entry.getValue());
			}

			ldpw.close();

			for (Map.Entry<String, Integer> entry : wordDict.entrySet()) {
				wdpw.println(entry.getKey() + "\001" + entry.getValue());
			}

			wdpw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void featureCount(String preprocess, String featureAnalysis) {

		boolean ngram = true;

		try {

			BufferedReader br = new BufferedReader(new FileReader(preprocess));

			String label = "", words = "", line = "";

			String[] fields = null;
			String[] tokens = null;

			ArrayList<String> doc = null;
			HashMap<String, Integer> docMap = null;

			int iter = 0;
			List<WORD> wordList = null;
			WORD word = null;

			while ((line = br.readLine()) != null) {

				if (SSO.tioe(line)) {
					continue;
				}

				doc = new ArrayList<String>();
				docMap = new HashMap<String, Integer>();

				fields = line.split("\001");

				if (fields.length != 2) {
					continue;
				}

				label = fields[0].trim();
				words = fields[1];

				wordList = processLine(words);

				for (int s = 0; s < wordList.size(); s++) {

					word = wordList.get(s);

					if (!(wordDict.containsKey(word.key))) {
						wordDict.put(word.key, wordDict.size() + 1);
					}
				}

				iter++;

			}

			br.close();

			PrintWriter wdpw = new PrintWriter(new FileWriter(featureAnalysis));

			int nonzero = 0;
			int aboveone = 0;
			for (Map.Entry<String, Integer> entry : wordDict.entrySet()) {

				if (entry.getValue() > 0) {
					nonzero++;
				}

				if (entry.getValue() > 1) {
					aboveone++;
				}

				wdpw.println(entry.getKey() + "\001" + entry.getValue());
			}

			wdpw.close();

			System.err.println("nonzero:" + nonzero);
			System.err.println("aboveone:" + aboveone);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadDict(String word_dict, String label_dict) {

		try {

			BufferedReader wbr = new BufferedReader(new FileReader(word_dict));
			String line = "", key = "";
			int value = 0;
			String[] tokens = null;

			while ((line = wbr.readLine()) != null) {
				if (SSO.tioe(line)) {
					continue;
				}

				line = line.trim();
				tokens = line.split("\001");

				if (tokens == null || tokens.length != 2) {
					continue;
				}

				key = tokens[0];
				value = Integer.parseInt(tokens[1]);

				if (SSO.tioe(key)) {
					continue;
				}

				wordDict.put(key, value);
			}

			wbr.close();

			BufferedReader lbr = new BufferedReader(new FileReader(label_dict));

			while ((line = lbr.readLine()) != null) {

				if (SSO.tioe(line)) {
					continue;
				}

				line = line.trim();
				tokens = line.split("\001");

				if (tokens == null || tokens.length != 2) {
					continue;
				}

				key = tokens[0];
				value = Integer.parseInt(tokens[1]);

				if (SSO.tioe(key)) {
					continue;
				}

				labelDict.put(key, value);
			}

			lbr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * convert to sample
	 * 
	 * @param preprocess
	 * @param format
	 */
	public void genSample(String preprocess, String format) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(preprocess));
			PrintWriter pw = new PrintWriter(format);

			String label = "", words = "", line = "";

			String[] fields = null;
			String[] tokens = null;

			int labelIndex;
			int wordIndex;
			ArrayList<String> wordSimpleList = null;

			String[] sortArr = null;

			String formatLine = "";

			int c = 0;
			List<WORD> wordList = null;
			WORD word = null;

			while ((line = br.readLine()) != null) {

				if (SSO.tioe(line)) {
					continue;
				}

				c++;

				if (c % 10000 == 0) {
					System.err.println(c + " lines has been converted ");
				}

				fields = line.split("\001");

				if (fields.length != 2) {
					continue;
				}

				label = fields[0].trim();
				words = fields[1];

				wordList = processLine(words);

				labelIndex = 0;
				labelIndex = labelDict.get(label);
				if (labelIndex < 1) {
					continue;
				}
				formatLine = labelIndex + " ";

				wordSimpleList = new ArrayList<String>();
				for (int k = 0; k < wordList.size(); k++) {
					word = wordList.get(k);
					if (wordDict.containsKey(word.key)) {
						wordIndex = wordDict.get(word.key);
						wordSimpleList.add(wordIndex + "\001" + word.value);
					}
				}

				sortArr = SortStrArray.sort_List(wordSimpleList, 0, "int", 2,
						"\001");

				for (int k = sortArr.length - 1; k >= 0; k--) {
					formatLine += (sortArr[k].split("\001")[0] + ":"
							+ sortArr[k].split("\001")[1] + " ");
				}

				formatLine = formatLine.trim();

				pw.println(formatLine);

			}

			br.close();

			pw.close();

		} catch (Exception e) {

		}

	}

	/**
	 * convert to sample (train and test)
	 * 
	 * @param preprocess
	 * @param format
	 */
	public void genSampleTrainTest(String format, String train, String test) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(format));
			PrintWriter trainPw = new PrintWriter(train);
			PrintWriter testPw = new PrintWriter(test);

			String line = "";

			while ((line = br.readLine()) != null) {
				if (SSO.tioe(line)) {
					continue;
				}

				if (Math.random() > 0.7) {
					testPw.println(line);
				} else {
					trainPw.println(line);
				}

			}

			br.close();
			trainPw.close();
			testPw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

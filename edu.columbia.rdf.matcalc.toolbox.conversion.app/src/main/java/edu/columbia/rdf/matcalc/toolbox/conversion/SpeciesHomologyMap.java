package edu.columbia.rdf.matcalc.toolbox.conversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jebtk.core.Resources;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.DefaultHashMapCreator;
import org.jebtk.core.collections.EntryCreator;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.text.Splitter;
import org.jebtk.core.text.TextUtils;
import org.xml.sax.SAXException;

public class SpeciesHomologyMap {
	
	private Map<Integer, IterMap<Integer, HomologyMap>> mConversionMap;
	
	public SpeciesHomologyMap() {
		mConversionMap = DefaultHashMap.create(new DefaultHashMapCreator<Integer, HomologyMap>(new EntryCreator<HomologyMap>(){
			@Override
			public HomologyMap newEntry() {
				return new HomologyMap();
			}}));
	}
	
	public HomologyMap humanToMouse() {
		return mConversionMap.get(GenesService.HUMAN_TAX_ID).get(GenesService.MOUSE_TAX_ID);
	}
	
	public HomologyMap mouseToHuman() {
		return mConversionMap.get(GenesService.MOUSE_TAX_ID).get(GenesService.HUMAN_TAX_ID);
	}
	
	public HomologyMap getMap(int fromTaxId, int toTaxId) {
		return mConversionMap.get(fromTaxId).get(toTaxId);
	}
	
	
	public void loadNcbi(final Path file) throws IOException {
		BufferedReader reader = Resources.getGzipReader(file);
		String line;
		List<String> tokens;

		String humanEntrez = null;
		String humanSymbol = null;
		String humanRefseq = null;
		String mouseEntrez = null;
		String mouseSymbol = null;
		String mouseRefseq = null;
		String hid = null;

		try {
			// Skip header
			reader.readLine();

			while ((line = reader.readLine()) != null) {
				tokens = Splitter.onTab().text(line);
				
				//taxId = Parser.toInt(tokens.get(1));

				if (hid == null || !tokens.get(0).equals(hid)) {
					// Each time we encounter a new species group, reset
					// the mapping
					humanEntrez = null;
					humanSymbol = null;
					humanRefseq = null;
					mouseEntrez = null;
					mouseSymbol = null;
					mouseRefseq = null;

					hid = tokens.get(0);
				}

				// Human
				if (tokens.get(1).equals("9606")) {
					humanEntrez = tokens.get(2);
					humanSymbol = tokens.get(3).toLowerCase();
					humanRefseq = tokens.get(5).toLowerCase().replaceFirst("\\..+", TextUtils.EMPTY_STRING);
				}

				// Mouse
				if (tokens.get(1).equals("10090")) {
					mouseEntrez = tokens.get(2);
					mouseSymbol = tokens.get(3).toLowerCase();
					mouseRefseq = tokens.get(5).toLowerCase().replaceFirst("\\..+", TextUtils.EMPTY_STRING);
				}


				if (humanSymbol != null && mouseSymbol != null) {
					// If both the human and mouse appear within a group,
					// create a mapping between them
					getMap(GenesService.HUMAN_TAX_ID, GenesService.MOUSE_TAX_ID).addMapping(humanSymbol, mouseSymbol);
					getMap(GenesService.HUMAN_TAX_ID, GenesService.MOUSE_TAX_ID).addMapping(humanEntrez, mouseSymbol);
					getMap(GenesService.HUMAN_TAX_ID, GenesService.MOUSE_TAX_ID).addMapping(humanRefseq, mouseSymbol);
					getMap(GenesService.MOUSE_TAX_ID, GenesService.HUMAN_TAX_ID).addMapping(mouseSymbol, humanSymbol);
					getMap(GenesService.MOUSE_TAX_ID, GenesService.HUMAN_TAX_ID).addMapping(mouseEntrez, humanSymbol);
					getMap(GenesService.MOUSE_TAX_ID, GenesService.HUMAN_TAX_ID).addMapping(mouseRefseq, humanSymbol);
				}
			}
		} finally {
			reader.close();
		}
	}
	
	public static void parseGenesXmlGz(Path file, 
			SpeciesHomologyMap speciesMap) throws IOException, ParserConfigurationException, SAXException {
		InputStream stream = Resources.getGzipInputStream(file);
		
		try {
			parseHomologyXml(stream, speciesMap);
		} finally {
			stream.close();
		}
	}

	public static void parseHomologyXml(InputStream is, 
			SpeciesHomologyMap speciesMap) throws ParserConfigurationException, SAXException, IOException {
		if (is == null) {
			return;
		}

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();

		HomologyXmlHandler handler = new HomologyXmlHandler(speciesMap);

		saxParser.parse(is, handler);
	}
}

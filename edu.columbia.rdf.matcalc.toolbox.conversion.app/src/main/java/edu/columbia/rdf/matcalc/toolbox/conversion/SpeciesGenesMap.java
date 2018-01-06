package edu.columbia.rdf.matcalc.toolbox.conversion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jebtk.core.Resources;
import org.xml.sax.SAXException;

public class SpeciesGenesMap {
  private Map<Integer, GenesMap> mSpeciesMap = new HashMap<Integer, GenesMap>();

  public GenesMap getMap(int taxId) {
    if (!mSpeciesMap.containsKey(taxId)) {
      switch (taxId) {
      case GenesService.MOUSE_TAX_ID:
        mSpeciesMap.put(taxId, new GenesMap("mouse"));
        break;
      default:
        mSpeciesMap.put(taxId, new GenesMap("human"));
        break;
      }
    }

    return mSpeciesMap.get(taxId);
  }

  public static void parseGenesXmlGz(Path file, SpeciesGenesMap speciesMap)
      throws IOException, ParserConfigurationException, SAXException {
    InputStream stream = Resources.getGzipInputStream(file);

    try {
      parseGenesXml(stream, speciesMap);
    } finally {
      stream.close();
    }
  }

  public static void parseGenesXml(InputStream is, SpeciesGenesMap speciesMap)
      throws ParserConfigurationException, SAXException, IOException {
    if (is == null) {
      return;
    }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    GenesXmlHandler handler = new GenesXmlHandler(speciesMap);

    saxParser.parse(is, handler);
  }
}

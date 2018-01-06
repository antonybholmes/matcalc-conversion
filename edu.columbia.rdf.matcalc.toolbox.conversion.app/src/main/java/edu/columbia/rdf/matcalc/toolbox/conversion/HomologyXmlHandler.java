/**
 * Copyright (C) 2016, Antony Holmes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of copyright holder nor the names of its contributors 
 *     may be used to endorse or promote products derived from this software 
 *     without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.columbia.rdf.matcalc.toolbox.conversion;

import java.text.ParseException;
import java.util.Map;

import org.jebtk.core.collections.DefaultTreeMap;
import org.jebtk.core.collections.DefaultTreeMapCreator;
import org.jebtk.core.collections.IterMap;
import org.jebtk.core.collections.TreeMapCreator;
import org.jebtk.core.text.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The class KeyXmlHandler.
 */
public class HomologyXmlHandler extends DefaultHandler {

  private String mSymbol;
  private SpeciesHomologyMap mSpeciesMap;
  private Map<Integer, IterMap<String, IterMap<String, String>>> mIdMap;

  private int mTaxId;

  public HomologyXmlHandler(SpeciesHomologyMap speciesMap) {
    mSpeciesMap = speciesMap;

    mIdMap = new DefaultTreeMap<Integer, IterMap<String, IterMap<String, String>>>(
        new DefaultTreeMapCreator<String, IterMap<String, String>>(new TreeMapCreator<String, String>()));

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
   * java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equals("group")) {
      mIdMap.clear();
    } else if (qName.equals("gene")) {
      mSymbol = attributes.getValue("symbol");

      try {
        mTaxId = Parser.toInt(attributes.getValue("tax_id"));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      mIdMap.get(mTaxId).get(mSymbol).put("symbol", mSymbol);

      if (mSymbol.toLowerCase().contains("coro7")) {
        System.err.println("aha " + mSymbol + " " + mTaxId);
      }

    } else if (qName.equals("id")) {
      String name = attributes.getValue("name");
      String type = attributes.getValue("type").toLowerCase();

      mIdMap.get(mTaxId).get(mSymbol).put(type, name);
    } else {
      // Do nothing
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("group")) {

      for (int taxId1 : mIdMap.keySet()) {
        for (String symbol1 : mIdMap.get(taxId1).keySet()) {
          for (String type1 : mIdMap.get(taxId1).get(symbol1).keySet()) {
            for (int taxId2 : mIdMap.keySet()) {
              for (String symbol2 : mIdMap.get(taxId2).keySet()) {
                for (String type2 : mIdMap.get(taxId2).get(symbol2).keySet()) {
                  /*
                   * String id1 = symbol1.toLowerCase(); String id2 = symbol2.toLowerCase();
                   * 
                   * mSpeciesMap.getMap(taxId1, taxId2).addMapping(id1, id2);
                   * mSpeciesMap.getMap(taxId2, taxId1).addMapping(id2, id1);
                   * 
                   * if (id1.contains("coro7") || id2.contains("coro7")) {
                   * System.err.println("coro " + taxId1 + " " + id1 + " " + taxId2 + " " + id2);
                   * }
                   */

                  String id1 = mIdMap.get(taxId1).get(symbol1).get(type1).toLowerCase();
                  String id2 = symbol2.toLowerCase(); // mIdMap.get(taxId2).get(symbol2).get(type2).toLowerCase();

                  mSpeciesMap.getMap(taxId1, taxId2).addMapping(id1, id2);
                  // mSpeciesMap.getMap(taxId2, taxId1).addMapping(id2, id1);
                }
              }
            }
          }
        }
      }

      /*
       * mSpeciesMap.getMap(mTaxId, mTaxId2).addMapping(mSymbol, mSymbol2);
       * 
       * for (String type : mIdMap.keySet()) { mSpeciesMap.getMap(mTaxId,
       * mTaxId2).addMapping(mIdMap.get(type), mSymbol2); }
       * 
       * mSpeciesMap.getMap(mTaxId2, mTaxId).addMapping(mSymbol2, mSymbol);
       * 
       * for (String type : mIdMap2.keySet()) { mSpeciesMap.getMap(mTaxId2,
       * mTaxId).addMapping(mIdMap2.get(type), mSymbol); }
       */
    }
  }
}

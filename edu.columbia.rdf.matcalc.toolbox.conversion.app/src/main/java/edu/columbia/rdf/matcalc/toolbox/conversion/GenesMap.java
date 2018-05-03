/**
 * Copyright 2018 Antony Holmes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.columbia.rdf.matcalc.toolbox.conversion;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.collections.DefaultHashMap;
import org.jebtk.core.collections.EntryCreator;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.Splitter;
import org.jebtk.core.text.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class GenesMap.
 */
public class GenesMap {

  /** The Constant SYMBOL_TYPE. */
  public static final String SYMBOL_TYPE = "symbol";

  /** The Constant ENTREZ_TYPE. */
  public static final String ENTREZ_TYPE = "entrez";

  /** The Constant REFSEQ_TYPE. */
  public static final String REFSEQ_TYPE = "refseq";

  /** The Constant ENSEMBL_GENE_TYPE. */
  public static final String ENSEMBL_GENE_TYPE = "ensembl-gene";

  /** The Constant ENSEMBL_TRANSCRIPT_TYPE. */
  public static final String ENSEMBL_TRANSCRIPT_TYPE = "ensembl-transcript";

  /** The Constant CHR_TYPE. */
  public static final String CHR_TYPE = "chr";

  /** The Constant STRAND_TYPE. */
  public static final String STRAND_TYPE = "strand";

  /** The m id to symbol map. */
  private Map<String, String> mIdToSymbolMap = new HashMap<String, String>();

  /** The m alt id to symbol map. */
  private Map<String, String> mAltIdToSymbolMap = new HashMap<String, String>();

  /** The m old id to symbol map. */
  private Map<String, String> mOldIdToSymbolMap = new HashMap<String, String>();

  /** The m offical id map. */
  private Map<String, GeneMapping> mOfficalIdMap;

  /**
   * Instantiates a new genes map.
   *
   * @param species the species
   */
  public GenesMap(String species) {
    mOfficalIdMap = DefaultHashMap.create(new EntryCreator<GeneMapping>() {
      @Override
      public GeneMapping newEntry() {
        return new GeneMapping();
      }
    });
  }

  /**
   * Adds the mapping.
   *
   * @param symbol the symbol
   * @param name the name
   * @param type the type
   */
  public void addMapping(String symbol, String name, String type) {
    String ls = santize(symbol);

    mIdToSymbolMap.put(ls, ls);

    mIdToSymbolMap.put(santize(name), ls);

    mOfficalIdMap.get(ls).getIds(type).add(name);
    mOfficalIdMap.get(ls).getIds(SYMBOL_TYPE).add(symbol);
  }

  /**
   * Adds the old mapping.
   *
   * @param symbol the symbol
   * @param name the name
   */
  public void addOldMapping(String symbol, String name) {
    mOldIdToSymbolMap.put(santize(name), santize(symbol));
  }

  /**
   * Add an unofficial name mapping. These are considered still valid and not
   * retired.
   *
   * @param symbol the symbol
   * @param name the name
   */
  public void addAltMapping(String symbol, String name) {
    mAltIdToSymbolMap.put(santize(name), santize(symbol));
  }

  /**
   * Updates a symbol/id to what is the current up to date symbol.
   *
   * @param c the c
   * @param split the split
   * @param symbols the symbols
   */
  public void convert(Conversion c, boolean split, Set<Conversion> symbols) {
    convert(c, split, null, symbols);
  }

  /**
   * Convert symbol also checking that if the symbol is an old symbol that is
   * converted to a new symbol, the new symbol does not have a homology since
   * homology is used as the gold standard. Thus is there is a homology between
   * species, the conversion must be by homology only and not simple name
   * similarity mapping.
   *
   * @param c the c
   * @param split the split
   * @param conversionMap the conversion map
   * @param symbols the symbols
   */
  public void convert(Conversion c,
      boolean split,
      HomologyMap conversionMap,
      Set<Conversion> symbols) {

    String id = santize(c.getId());
    String chr = c.getChr();

    if (mIdToSymbolMap.containsKey(id)) {
      String newId = mIdToSymbolMap.get(id);

      if (chrCheck(newId, chr)) {
        c = new Conversion(newId, c, "map:" + newId);

        symbols.add(c);

        return;
      }
    }

    // OK, might be an unofficial alternative symbol name

    // As a backup, check the old symbols
    if (mAltIdToSymbolMap.containsKey(id)) {
      String newId = mAltIdToSymbolMap.get(id);

      if (chrCheck(newId, chr)) {
        c = new Conversion(newId, c, "alt:" + newId);

        if (conversionMap == null || !conversionMap.contains(newId)) {
          symbols.add(c);
        } else {
          symbols.add(new Conversion(TextUtils.NA, c, "hom-err"));
        }

        /*
         * if (conversionMap == null || !conversionMap.contains(newId)) {
         * symbols.add(c); } else { symbols.add(new Conversion(TextUtils.NA, c,
         * "hom-err")); }
         */

        return;
      }
    }

    // OK, might be an old symbol name

    // As a backup, check the old symbols
    if (mOldIdToSymbolMap.containsKey(id)) {
      String newId = mOldIdToSymbolMap.get(id);

      if (chrCheck(newId, chr)) {
        c = new Conversion(newId, c, "old:" + newId);

        if (conversionMap == null || !conversionMap.contains(newId)) {
          symbols.add(c);
        } else {
          symbols.add(new Conversion(TextUtils.NA, c, "hom-err"));
        }

        return;
      }
    }

    // See if its a loc problem

    if (id.startsWith("loc")) {
      id = id.substring(3);

      c = new Conversion(id, c, "loc:" + id);

      if (mIdToSymbolMap.containsKey(id)) {
        String newId = mIdToSymbolMap.get(id);

        symbols.add(new Conversion(newId, c, "map:" + newId));
        return;
      } else {
        if (mOldIdToSymbolMap.containsKey(id)) {
          String newId = mOldIdToSymbolMap.get(id);

          c = new Conversion(newId, c, "old:" + newId);

          if (conversionMap == null || !conversionMap.contains(newId)) {
            symbols.add(c);
          } else {
            symbols.add(new Conversion(TextUtils.NA, c, "hom-err"));
          }

          return;
        }
      }
    }

    if (split) {
      // Consider splitting the id to see if it is made up
      // of a two gene symbols separated by a dash

      List<String> terms = Splitter.onDash().text(id);

      // We ignore anti-sense cases
      for (String term : terms) {
        if (term.endsWith("as1") || term.endsWith("as2")) {
          return;
        }
      }

      // Each term must be its own mappable term
      for (String term : terms) {
        if (!mIdToSymbolMap.containsKey(term)) {
          return;
        }
      }

      // all terms must be on the same chr
      /*
       * String chr = null;
       * 
       * for (String term : terms) { String s = mIdToSymbolMap.get(term);
       * 
       * if (chr == null) { chr = symbolChrMap.get(s); }
       * 
       * if (!symbolChrMap.get(s).equals(chr)) { return; } }
       */

      for (String term : terms) {
        if (mIdToSymbolMap.containsKey(term)) {
          String newId = mIdToSymbolMap.get(term);

          //symbols.add(new Conversion(newId, c, "split:" + newId));

          // recursively update
          convert(new Conversion(newId, c, "split:" + newId),
              split,
              conversionMap,
              symbols);
        }
      }
    }
  }

  /**
   * If chr data available, check we are on the right chr.
   * 
   * @param id
   * @param chr
   * @return
   */
  private boolean chrCheck(String id, String chr) {
    if (chr == null) {
      return true;
    }

    String c = getChr(id);

    if (c == null) {
      return true;
    }

    return c.equals(chr);
  }

  /**
   * Gets the symbols.
   *
   * @param c the c
   * @return the symbols
   */
  public Collection<String> getSymbols(Conversion c) {
    return getSymbols(c.getId());
  }

  /**
   * Gets the symbols.
   *
   * @param id the id
   * @return the symbols
   */
  public Collection<String> getSymbols(String id) {
    return getMappings(id, SYMBOL_TYPE);
  }

  /**
   * Gets the symbols.
   *
   * @param ids the ids
   * @return the symbols
   */
  public Collection<String> getSymbols(final Set<Conversion> ids) {
    return getMappings(ids, SYMBOL_TYPE);
  }

  /**
   * Gets the entrez.
   *
   * @param c the c
   * @return the entrez
   */
  public Collection<String> getEntrez(Conversion c) {
    return getEntrez(c.getId());
  }

  /**
   * Gets the entrez.
   *
   * @param id the id
   * @return the entrez
   */
  public Collection<String> getEntrez(String id) {
    return getMappings(id, ENTREZ_TYPE);
  }

  /**
   * Gets the refseq.
   *
   * @param c the c
   * @return the refseq
   */
  public Collection<String> getRefseq(Conversion c) {
    return getRefseq(c.getId());
  }

  /**
   * Gets the refseq.
   *
   * @param id the id
   * @return the refseq
   */
  public Collection<String> getRefseq(String id) {
    return getMappings(id, REFSEQ_TYPE);
  }

  public String getChr(Conversion c) {
    return getChr(c.getId());
  }

  /**
   * Gets the refseq.
   *
   * @param id the id
   * @return the refseq
   */
  public String getChr(String id) {
    return getMapping(id, CHR_TYPE);
  }

  public String getMapping(String id, String type) {
    Collection<String> mappings = getMappings(id, type);

    if (mappings.size() > 0) {
      return mappings.iterator().next();
    } else {
      return TextUtils.EMPTY_STRING;
    }
  }

  /**
   * For a given id, return any mappings of a given type. type will be of the
   * form "entrez", "refseq", "ensembl-gene" etc depending on the data sources
   * available.
   *
   * @param id the id
   * @param type the type
   * @return the mappings
   */
  public Collection<String> getMappings(String id, String type) {
    return mOfficalIdMap.get(santize(id)).getIds(type);
  }

  /**
   * Gets the mappings.
   *
   * @param c the c
   * @param type the type
   * @return the mappings
   */
  public Collection<String> getMappings(Conversion c, String type) {
    return getMappings(c.getId(), type);
  }

  /**
   * Convert ids to real ids.
   *
   * @param ids the ids
   * @param type the type
   * @return the mappings
   */
  public Set<String> getMappings(final Collection<Conversion> ids, String type) {
    Set<String> officalSymbols = new TreeSet<String>();

    for (Conversion c : ids) {
      officalSymbols.addAll(getMappings(c.getId(), type));
    }

    return officalSymbols;
  }

  /**
   * To string.
   *
   * @param symbols the symbols
   * @param type the type
   * @return the string
   */
  public String toString(final Set<Conversion> symbols, String type) {
    return Join.onSemiColon()
        .values(CollectionUtils.sort(getMappings(symbols, type))).toString();
  }

  private static final String santize(String v) {
    return v.toLowerCase();
  }

  /*
   * public void loadEnsembl(final Path file) throws IOException {
   * loadEnsembl(file, this); }
   * 
   * public static void loadEnsembl(final Path file, GenesMap genesMap) throws
   * IOException { String line; List<String> tokens;
   * 
   * String id1; String id2; String symbol;
   * 
   * BufferedReader reader = Resources.getGzipReader(file);
   * 
   * Splitter splitter = Splitter.onTab();
   * 
   * try { reader.readLine();
   * 
   * while ((line = reader.readLine()) != null) { tokens = splitter.text(line);
   * 
   * id1 = tokens.get(0); id2 = tokens.get(1); symbol = tokens.get(2); //ls =
   * symbol.toLowerCase(); //chr = tokens.get(3); //strand = tokens.get(4);
   * 
   * genesMap.addMapping(symbol, id1, "ensembl_transcript");
   * genesMap.addMapping(symbol, id2, "ensembl_gene");
   * 
   * } } finally { reader.close(); } }
   */
}

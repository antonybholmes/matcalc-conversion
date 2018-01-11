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

public class GenesMap {
  public static final String SYMBOL_TYPE = "symbol";

  public static final String ENTREZ_TYPE = "entrez";

  public static final String REFSEQ_TYPE = "refseq";

  public static final String ENSEMBL_GENE_TYPE = "ensembl-gene";

  public static final String ENSEMBL_TRANSCRIPT_TYPE = "ensembl-transcript";

  public static final String CHR_TYPE = "chr";

  public static final String STRAND_TYPE = "strand";

  private Map<String, String> mIdToSymbolMap = new HashMap<String, String>();

  private Map<String, String> mAltIdToSymbolMap = new HashMap<String, String>();

  private Map<String, String> mOldIdToSymbolMap = new HashMap<String, String>();

  private Map<String, GeneMapping> mOfficalIdMap;

  public GenesMap(String species) {
    mOfficalIdMap = DefaultHashMap.create(new EntryCreator<GeneMapping>() {
      @Override
      public GeneMapping newEntry() {
        return new GeneMapping();
      }
    });
  }

  public void addMapping(String symbol, String name, String type) {
    String ls = symbol.toLowerCase();

    mIdToSymbolMap.put(ls, ls);

    mIdToSymbolMap.put(name.toLowerCase(), ls);

    mOfficalIdMap.get(ls).getIds(type).add(name);
    mOfficalIdMap.get(ls).getIds(SYMBOL_TYPE).add(symbol);
  }

  public void addOldMapping(String symbol, String name) {
    String ls = symbol.toLowerCase();

    mOldIdToSymbolMap.put(name.toLowerCase(), ls);
  }

  /**
   * Add an unofficial name mapping. These are considered still valid and not
   * retired.
   * 
   * @param symbol
   * @param name
   */
  public void addAltMapping(String symbol, String name) {
    String ls = symbol.toLowerCase();

    mAltIdToSymbolMap.put(name.toLowerCase(), ls);
  }

  /**
   * Updates a symbol/id to what is the current up to date symbol.
   * 
   * @param c
   * @param split
   * @param symbols
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
   * @param c
   * @param split
   * @param conversionMap
   * @param symbols
   */
  public void convert(Conversion c,
      boolean split,
      HomologyMap conversionMap,
      Set<Conversion> symbols) {

    String id = c.getId();

    if (mIdToSymbolMap.containsKey(id)) {
      String newId = mIdToSymbolMap.get(id);

      c = new Conversion(newId, c, "map:" + newId);

      symbols.add(c);

      return;
    }

    // OK, might be an unofficial alternative symbol name

    // As a backup, check the old symbols
    if (mAltIdToSymbolMap.containsKey(id)) {
      String newId = mAltIdToSymbolMap.get(id);

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

    // OK, might be an old symbol name

    // As a backup, check the old symbols
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

          symbols.add(new Conversion(newId, c, "split:" + newId));
        }
      }
    }
  }

  public Collection<String> getSymbols(Conversion c) {
    return getSymbols(c.getId());
  }

  public Collection<String> getSymbols(String id) {
    return getMappings(id, SYMBOL_TYPE);
  }

  public Collection<String> getSymbols(final Set<Conversion> ids) {
    return getMappings(ids, SYMBOL_TYPE);
  }

  public Collection<String> getEntrez(Conversion c) {
    return getEntrez(c.getId());
  }

  public Collection<String> getEntrez(String id) {
    return getMappings(id, ENTREZ_TYPE);
  }

  public Collection<String> getRefseq(Conversion c) {
    return getRefseq(c.getId());
  }

  public Collection<String> getRefseq(String id) {
    return getMappings(id, REFSEQ_TYPE);
  }

  /**
   * For a given id, return any mappings of a given type. type will be of the
   * form "entrez", "refseq", "ensembl-gene" etc depending on the data sources
   * available.
   * 
   * @param id
   * @param type
   * @return
   */
  public Collection<String> getMappings(String id, String type) {
    return mOfficalIdMap.get(id).getIds(type);
  }

  public Collection<String> getMappings(Conversion c, String type) {
    return getMappings(c.getId(), type);
  }

  public Set<String> getMappings(final Set<Conversion> ids, String type) {
    Set<String> officalSymbols = new TreeSet<String>();

    for (Conversion c : ids) {
      officalSymbols.addAll(getMappings(c.getId(), type));
    }

    return officalSymbols;
  }

  public String toString(final Set<Conversion> symbols, String type) {
    return Join.onSemiColon()
        .values(CollectionUtils.sort(getMappings(symbols, type))).toString();
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

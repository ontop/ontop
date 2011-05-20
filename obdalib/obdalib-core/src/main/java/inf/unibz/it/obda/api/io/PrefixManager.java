package inf.unibz.it.obda.api.io;

import java.util.HashMap;

/**
 * Abstracts the prefix mapping mechanism.
 * 
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public interface PrefixManager
{
  public void addUri(String uri, String prefix);
  
  public String getURIForPrefix(String prefix);
  
  public String getPrefixForURI(String uri);
  
  public HashMap<String, String> getPrefixMap();
}

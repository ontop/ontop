package it.unibz.krdb.obda.io;

import java.io.Serializable;
import java.util.Map;

/**
 * Abstracts the prefix mapping mechanism.
 * 
 * @author Josef Hardi <josef.hardi@gmail.com>
 */
public interface PrefixManager extends Serializable
{
  public void addUri(String uri, String prefix);
  
  public String getURIForPrefix(String prefix);
  
  public String getPrefixForURI(String uri);
  
  public Map<String, String> getPrefixMap();
  
  public String getDefaultNamespace();
  
  public void setDefaultNamespace(String uri);
  
  public String getShortForm(String uri, boolean useDefaultPrefix);
  
  public String getShortForm(String uri);
}

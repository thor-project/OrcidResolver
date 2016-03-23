/*
 * Copyright (c) PANGAEA - Data Publisher for Earth & Environmental Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.pangaea.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <p>
 * Title: OrcidResolver
 * </p>
 * <p>
 * Description: Returns Orcid after looking up name and a list of DOIs.
 * <em>Please note:</em> Instances of this class are <b>not<b> thread safe!
 * </p>
 * <p>
 * Project: THOR
 * </p>
 * <p>
 * Copyright: PANGAEA
 * </p>
 */

public class OrcidResolver {
  
  public static final String ORCID_API_URL = "http://pub.orcid.org/v1.2";
  public static final String ORCID_API_PATH = "/search/orcid-bio/";
  public static final String ORCID_API_QUERY = "?start=0&rows=1&q.op=OR&q=";

  @SuppressWarnings("serial")
  private static final BitSet SOLR_ESCAPE_CHARS = new BitSet() {{
    final String escapes = "+-&|!(){}[]^\"~*?:\\/";
    for (int i = 0, c = escapes.length(); i < c; i++) {
      set(escapes.charAt(i));
    }
  }};
  
  private final DocumentBuilderFactory dbf;
  private final XPathExpression numFoundPath, orcidPath;
  
  public OrcidResolver() {
    try {
      dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
    } catch (Exception e) {
      throw new RuntimeException("Failed to instantiate DocumentBuilderFactory, this may be caused by invalid XML configuration.", e);
    }
    
    final Map<String, String> prefixToNS = new HashMap<>();
    prefixToNS.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    prefixToNS.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    prefixToNS.put("orcid", "http://www.orcid.org/ns/orcid");
    
    final XPath x = XPathFactory.newInstance().newXPath();
    x.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {
        Objects.requireNonNull(prefix, "Namespace prefix cannot be null");
        final String uri = prefixToNS.get(prefix);
        if (uri == null) {
          throw new IllegalArgumentException("Undeclared namespace prefix: " + prefix);
        }
        return uri;
      }
      
      @Override
      public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
      }
      
      @Override
      public Iterator<?> getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
      }
    });
    
    try {
      numFoundPath = x.compile("//orcid:orcid-search-results/@num-found");
      orcidPath = x.compile("//orcid:orcid-profile[1]/orcid:orcid-identifier/orcid:path");
    } catch (XPathException e) {
      throw new RuntimeException("Failed to compile XPath, this may be caused by invalid XML configuration.", e);
    }
  }
  
  public String resolve(String lastName, String firstName, String... dois) throws IOException {
    return resolve(lastName, firstName, Arrays.asList(dois));
  }
  
  public String resolve(String lastName, String firstName, Collection<String> dois) throws IOException {
    Objects.requireNonNull(lastName, "last name must not be null");
    Objects.requireNonNull(firstName, "first name must not be null");
    Objects.requireNonNull(dois, "dois name must not be null");
    
    if (dois.isEmpty()) {
      throw new IllegalArgumentException("dois must contain at least one DOI");
    }
    
    final String solrQuery = createSolrQuery(lastName, firstName, dois);
    
    final String orcid = getOrcid(solrQuery);
    
    if (orcid == null) {
      // no ORCID found
      return null;
    }
    
    if (!OrcidValidator.isValid(orcid)) {
      throw new IllegalStateException("Invalid ORCID returned by service: " + orcid);
    }
    
    return orcid;
  }
  
  private String createSolrQuery(String lastName, String firstName, Collection<String> dois) {
    final String firstNamePattern;
    final StringBuilder sb = new StringBuilder();
    
    if (firstName != null && !firstName.trim().isEmpty()) {
      boolean first = true;
      for (final String part : firstName.split("\\s+")) {
        if (part.matches("[\\p{Lu}\\.{0,1}]+")) { // only upper case
          for (int i = 0, c = part.length(); i < c; i++) {
            if (part.charAt(i) == '.') {
              continue;
            }
            sb.append(first ? "+" : " ").append(part.charAt(i)).append("*");
            first = false;
          }
        } else {
          sb.append(first ? "+" : " ").append(escapeSolrTerm(part));
          first = false;
        }
      }
      firstNamePattern = sb.toString().trim();
    } else {
      firstNamePattern = "";
    }
    
    final String lastNamePattern = escapeSolrTerm(lastName.trim());
    final StringBuilder solrQuery = new StringBuilder();
    
    solrQuery.append("+((");
    solrQuery.append("+family-name:\"").append(lastNamePattern).append('"');
    if (!firstNamePattern.isEmpty()) {
      solrQuery.append(" +given-names:(").append(firstNamePattern).append(')');
    }
    solrQuery.append(") credit-name:(");
    solrQuery.append("+\"").append(lastNamePattern).append('"');
    if (!firstNamePattern.isEmpty()) {
      solrQuery.append(" +(").append(firstNamePattern).append(')');
    }
    solrQuery.append(") other-names:(");
    solrQuery.append("+\"").append(lastNamePattern).append('"');
    if (!firstNamePattern.isEmpty()) {
      solrQuery.append(" +(").append(firstNamePattern).append(')');
    }
    solrQuery.append("))");
    
    solrQuery.append(" +digital-object-ids:(");
    
    boolean firstDoi = true;
    for (final String doi : dois) {
      if (!firstDoi) {
        solrQuery.append(' ');
      }
      solrQuery.append('"').append(escapeSolrTerm(doi)).append('"');
      if (solrQuery.length() > 768) {
        break;
      }
    }
    
    solrQuery.append(')');
    
    return solrQuery.toString();
  }
  
  private String getOrcid(String solrQuery) throws IOException {
    final URL url = new URL(ORCID_API_URL + ORCID_API_PATH + ORCID_API_QUERY
        + URLEncoder.encode(solrQuery, StandardCharsets.UTF_8.name()));
    final URLConnection connection = url.openConnection();
    connection.addRequestProperty("Accept", "application/orcid+xml");
    
    final int numFound;
    final String orcid;
    
    try (final InputStream in = connection.getInputStream()) {
      final Document doc = dbf.newDocumentBuilder().parse(in);
      
      numFound = ((Number) numFoundPath.evaluate(doc, XPathConstants.NUMBER)).intValue();
      orcid = orcidPath.evaluate(doc).trim();
    } catch (SAXException | XPathExpressionException | ParserConfigurationException e) {
      throw new IllegalStateException("Failed to parse ORCID response.", e);
    }
      
    if (numFound > 1) {
      throw new IllegalStateException(
          String.format(Locale.ENGLISH, "Obtained more than one ORCID [numFound = %d]", numFound));
    }
    
    if (!orcid.isEmpty()) {
      return orcid;
    }
    
    if (numFound == 1) {
      throw new IllegalStateException("One ORCID result returned, but no id found in response.");
    }
    
    return null;
  }
  
  private static String escapeSolrTerm(String term) {
    final StringBuilder sb = new StringBuilder();
    
    for (int i = 0, c = term.length(); i < c; i++) {
      final char ch = term.charAt(i);
      if (SOLR_ESCAPE_CHARS.get(ch)) {
        sb.append('\\').append(ch);
      } else {
        sb.append(ch);
      }
    }
    
    return sb.toString().replace("AND", "\\A\\N\\D").replace("OR", "\\O\\R");
  }
  
}

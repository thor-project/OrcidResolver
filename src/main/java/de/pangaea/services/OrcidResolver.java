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
import java.util.Objects;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;

/**
 * <p>
 * Title: OrcidResolver
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Project: THOR
 * </p>
 * <p>
 * Copyright: Copyright (C) 2016
 * </p>
 * 
 * @author Uwe Schindler
 * @author Markus Stocker
 */

public class OrcidResolver {

  public static final String ORCID_API_URL = "http://pub.orcid.org/v1.2";
  public static final String ORCID_API_PATH = "/search/orcid-bio/";
  public static final String ORCID_API_QUERY = "?start=0&rows=1&q.op=OR";

  private static final DocumentBuilder docbuilder;
  private static final XPathExpression orcidPath;
  private static final BitSet escapeChars = new BitSet();

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

    String ret = null;

    try {
      URL url = new URL(ORCID_API_URL + ORCID_API_PATH + ORCID_API_QUERY + "&q="
          + URLEncoder.encode(solrQuery, StandardCharsets.UTF_8.name()));
      URLConnection connection = url.openConnection();
      connection.addRequestProperty("Accept", "application/orcid+xml");
      InputStream in = connection.getInputStream();
      Document doc = docbuilder.parse(in);
      ret = orcidPath.evaluate(doc).trim();
    } catch (SAXException | XPathExpressionException e) {
      throw new IllegalStateException("Failed to parse ORCID response.", e);
    }

    if (ret == null) {
      return null;
    }

    if (ret.isEmpty()) {
      return null;
    }

    if (!OrcidValidator.isValid(ret)) {
      throw new IllegalStateException("Invalid ORCID returned by service: " + ret);
    }

    return ret;
  }

  private String createSolrQuery(String lastName, String firstName, Collection<String> dois) {
    final String firstNamePattern;
    final StringBuilder sb = new StringBuilder();

    if (firstName != null && !firstName.trim().isEmpty()) {
      boolean first = true;
      for (final String part : firstName.split("\\s+")) {
        if (part.matches("\\p{Lu}+")) { // only upper case
          for (int i = 0, c = part.length(); i < c; i++) {
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
      if (!firstDoi)
        solrQuery.append(' ');
      solrQuery.append('"').append(escapeSolrTerm(doi)).append('"');
      if (solrQuery.length() > 768)
        break;
    }

    solrQuery.append(')');

    return solrQuery.toString();
  }

  private static String escapeSolrTerm(String term) {
    final StringBuilder sb = new StringBuilder();
    
    for (int i = 0, c = term.length(); i < c; i++) {
      final char ch = term.charAt(i);
      if (escapeChars.get(ch)) {
        sb.append('\\').append(ch);
      } else {
        sb.append(ch);
      }
    }
    
    return sb.toString().replace("AND", "\\A\\N\\D").replace("OR", "\\O\\R");
  }

  static {
    try {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      docbuilder = dbf.newDocumentBuilder();
    } catch (Exception e) {
      throw new Error("Failed to instantiate DocumentBuilder", e);
    }

    final Map<String, String> prefixToNS = ImmutableMap.of(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI,
        XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "orcid",
        "http://www.orcid.org/ns/orcid");

    final XPath x = XPathFactory.newInstance().newXPath();

    x.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String prefix) {
        if (prefix == null)
          throw new IllegalArgumentException("Namespace prefix cannot be null");
        String uri = prefixToNS.get(prefix);
        if (uri == null)
          throw new IllegalArgumentException("Undeclared namespace prefix: " + prefix);
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
      orcidPath = x.compile("//orcid:orcid-profile[1]/orcid:orcid-identifier/orcid:path");
    } catch (XPathException e) {
      throw new Error("Failed to compile XPath", e);
    }

    final String escapes = "+-&|!(){}[]^\"~*?:\\/";

    for (int i = 0, c = escapes.length(); i < c; i++) {
      escapeChars.set(escapes.charAt(i));
    }
  }

}

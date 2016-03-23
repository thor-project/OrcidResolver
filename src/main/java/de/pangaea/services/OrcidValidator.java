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

import java.util.regex.Pattern;

/**
 * <p>
 * Title: OrcidValidator
 * </p>
 * <p>
 * Description: Validates the format of an ORCID. The expected format is
 * 'DDDD-DDDD-DDDD-DDDC', whereby 'D' = digit and 'C' = checksum. The code is
 * based on
 * http://support.orcid.org/knowledgebase/articles/116780-structure-of-the-orcid
 * -identifier
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

public class OrcidValidator {
  
  private static final Pattern orcidPattern = Pattern
      .compile("\\d\\d\\d\\d\\-\\d\\d\\d\\d\\-\\d\\d\\d\\d\\-\\d\\d\\d[X\\d]");
  
  public static boolean isValid(String orcid) {
    if (orcid == null)
      return false;
    
    if (!orcidPattern.matcher(orcid).matches())
      return false;
    
    final int last = orcid.length() - 1;
    int total = 0;
    
    for (int i = 0; i < last; i++) {
      final char ch = orcid.charAt(i);
      if (ch != '-') {
        total = (total + ch - '0') * 2;
      }
    }
    
    final int remainder = total % 11;
    final int result = (12 - remainder) % 11;
    char lastDigit = (result == 10) ? 'X' : (char) ('0' + result);
    
    if (orcid.charAt(last) != lastDigit)
      return false;
    
    return true;
  }
  
}

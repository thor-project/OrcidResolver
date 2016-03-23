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

package de.pangaea.services.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.pangaea.services.OrcidResolver;

/**
 * <p>
 * Title: OrcidResolverTest
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

public class OrcidResolverTest {
  
  private final OrcidResolver r = new OrcidResolver();
  
  @Test
  public void test1() throws Exception {
    assertEquals("0000-0002-1900-4162",
        r.resolve("Schindler", "Uwe", "10.1016/j.cageo.2008.02.023", "10.2481/dsj.5.79", "10.1007/11551362_12",
            "10.2312/wdc-mare.2005.1", "10.2312/wdc-mare.2005.3", "10.1016/S0098-3004(02)00039-0",
            "10.1594/PANGAEA.854363", "10.1594/PANGAEA.760905", "10.1594/PANGAEA.760907"));
  }
  
  @Test
  public void test2() throws Exception {
    assertEquals("0000-0003-3096-6829",
        r.resolve("Diepenbroek", "Michael", "10.5194/essd-7-239-2015", "10.1016/j.cageo.2008.02.023",
            "10.2481/dsj.5.79", "10.2481/dsj.5.79", "10.1007/11551362_12", "10.2312/wdc-mare.2005.1",
            "10.2312/wdc-mare.2005.3", "10.1016/S0098-3004(01)00112-1", "10.1016/S0098-3004(02)00039-0",
            "10.1038/35106716", "10.2312/BzP_0122_1993", "10.1594/PANGAEA.779740", "10.1594/PANGAEA.734199",
            "10.1594/PANGAEA.269663", "10.1594/PANGAEA.314733", "10.1594/PANGAEA.760905",
            "10.1594/PANGAEA.760907", "10.1594/PANGAEA.219627", "10.1594/PANGAEA.219628",
            "10.1594/PANGAEA.219629", "10.1594/PANGAEA.104840"));
  }
  
  @Test
  public void test3() throws Exception {
    assertNull(r.resolve("Schindler", "Uwe", "10.1594/PANGAEA.104840", "10.2312/BzP_0122_1993"));
  }
  
  @Test
  public void test4() throws Exception {
    // Resolves on "also known as"
    assertEquals("0000-0002-9567-9460",
        r.resolve("Draut", "Amy", "10.1016/j.geomorph.2014.08.028", "10.2110/jsr.2013.79"));
  }
  
}

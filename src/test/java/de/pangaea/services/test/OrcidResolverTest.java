/*
 * Copyright (C) 2016 see CREDITS.txt
 * All rights reserved.
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

	private OrcidResolver r = new OrcidResolver();

	@Test
	public void test1() {
		assertEquals("0000-0002-1900-4162",
				r.resolve("Schindler", "Uwe", "10.1016/j.cageo.2008.02.023", "10.2481/dsj.5.79", "10.1007/11551362_12",
						"10.2312/wdc-mare.2005.1", "10.2312/wdc-mare.2005.3", "10.1016/S0098-3004(02)00039-0",
						"10.1594/PANGAEA.854363", "10.1594/PANGAEA.760905", "10.1594/PANGAEA.760907"));
	}

	@Test
	public void test2() {
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
	public void test3() {
		assertNull(r.resolve("Schindler", "Uwe", "10.1594/PANGAEA.104840", "10.2312/BzP_0122_1993"));
	}

	@Test
	public void test4() {
		// Resolves on "also known as"
		assertEquals("0000-0002-9567-9460",
				r.resolve("Draut", "Amy", "10.1016/j.geomorph.2014.08.028", "10.2110/jsr.2013.79"));
	}
	
	@Test
	public void test5() {
		assertNull(r.resolve(null, "Uwe", "10.1594/PANGAEA.104840", "10.2312/BzP_0122_1993"));
	}

	@Test
	public void test6() {
		assertNull(r.resolve("Schindler", null, "10.1594/PANGAEA.104840", "10.2312/BzP_0122_1993"));
	}
	
	@Test
	public void test7() {
		assertNull(r.resolve("Schindler", "Uwe"));
	}
	
}

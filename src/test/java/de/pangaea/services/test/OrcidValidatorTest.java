/*
 * Copyright (C) 2016 see CREDITS.txt
 * All rights reserved.
 */

package de.pangaea.services.test;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import de.pangaea.services.OrcidValidator;

/**
 * <p>
 * Title: OrcidValidatorTest
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

public class OrcidValidatorTest {

	@Test
	public void test1() {
		assertTrue(OrcidValidator.isValid("0000-0002-1900-4162"));
	}
	
	@Test
	public void test2() {
		assertTrue(OrcidValidator.isValid("0000-0003-3096-6829"));
	}
	
	@Test
	public void test3() {
		assertTrue(OrcidValidator.isValid("0000-0002-9567-9460"));
	}
	
	@Test
	public void test4() {
		assertFalse(OrcidValidator.isValid("0000-0002-1900-4160"));
	}
	
	@Test
	public void test5() {
		assertFalse(OrcidValidator.isValid(null));
	}
	
	@Test
	public void test6() {
		assertFalse(OrcidValidator.isValid("0000-0002-190-4162"));
	}
	
	@Test
	public void test7() {
		assertFalse(OrcidValidator.isValid("0000_0002_1900_4162"));
	}
	
}

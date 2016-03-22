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

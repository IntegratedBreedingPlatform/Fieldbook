/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.CropOntologyService;
import com.efficio.pojos.cropontology.CropTerm;
import com.efficio.pojos.cropontology.Ontology;

@Ignore(value ="BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
public class CropOntologyServiceTest extends AbstractBaseIntegrationTest {

	private static final Logger LOG = LoggerFactory.getLogger(CropOntologyServiceTest.class);

	@Autowired
	private CropOntologyService cropOntologyService;

	/**
	 * Test search terms.
	 */
	@Test
	public void testSearchTerms() {
		String query = "stem rust";
		List<CropTerm> cropTerms = this.cropOntologyService.searchTerms(query);
		Assert.assertNotNull(cropTerms);
		Assert.assertFalse(cropTerms.isEmpty());
		for (CropTerm cropTerm : cropTerms) {
			CropOntologyServiceTest.LOG.debug(cropTerm.toString());
		}
	}

	/**
	 * Test get ontology id by name.
	 */
	@Test
	public void testGetOntologyIdByName() {
		String name = "cassava";
		String cropId = this.cropOntologyService.getOntologyIdByName(name);
		Assert.assertEquals("CO_334", cropId);
	}

	/**
	 * Test get ontology id by name with null param.
	 */
	@Test
	public void testGetOntologyIdByNameWithNullParam() {
		String name = null;
		String cropId = this.cropOntologyService.getOntologyIdByName(name);
		Assert.assertNull(cropId);
	}

	/**
	 * Test get ontology id by name that does not exist.
	 */
	@Test(expected = HttpClientErrorException.class)
	public void testGetOntologyIdByNameThatDoesNotExist() {
		String name = "testing-doesnotexist-condition";
		String cropId = this.cropOntologyService.getOntologyIdByName(name);
		Assert.assertNull(cropId);
	}

	/**
	 * Test get ontologies by category.
	 */
	@Test
	public void testGetOntologiesByCategory() {
		String category = "010-089 General Germplasm Ontology";
		List<Ontology> ontologies = this.cropOntologyService.getOntologiesByCategory(category);
		Assert.assertNotNull(ontologies);
		Assert.assertFalse(ontologies.isEmpty());
		for (Ontology ontology : ontologies) {
			CropOntologyServiceTest.LOG.debug(ontology.toString());
		}
	}

}

package com.dms.useful;

import static org.junit.Assert.*;

import org.junit.Test;

public class UFBrasilTest {

	@Test
	public void deveConter27Estados() throws Exception {
		int expected = 27;

		assertEquals(expected, UFBrasil.values().length);
	}

	@Test
	public void deveObterACapitalDePernambuco() throws Exception {
		String expected = "Recife";

		assertEquals(expected, UFBrasil.PE.getCapital());
	}

	@Test
	public void deveSerDaRegiaoNordeste() throws Exception {
		String expected = "NORDESTE";

		assertEquals(expected, UFBrasil.PE.getRegiao());
	}

	@Test
	public void deveObterEstadoPernambuco() throws Exception {
		String expected = "Pernambuco";

		assertEquals(expected, UFBrasil.PE.getEstado());
	}
}

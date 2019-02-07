package com.dms.useful.event.listener;

import java.net.URI;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationListener;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.dms.useful.event.ResourceCreatedEvent;

/**
 * Esta classe adiciona um Location no Headers na resposta da requisição,
 * informando como acessar o novo recurso criado.
 * 
 * <pre>
 * Extender está num pacote <project package>.event.listener
 * colocando a anotação {@code &#064;Component}
 * </pre>
 * 
 * This class has some dependencies that were inserted on the pom.xml
 * 
 * @see https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
 * 
 * @author Diorgenes Morais
 * @since 1.0.1
 * 
 */
public abstract class ResourceCreatedListener<ID> implements ApplicationListener<ResourceCreatedEvent<ID>> {

	@Override
	public void onApplicationEvent(ResourceCreatedEvent<ID> event) {
		HttpServletResponse response = event.getResponse();
		ID id = event.getId();

		addHeaderLocation(response, id);
	}

	private void addHeaderLocation(HttpServletResponse response, ID id) {
		URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").build(id);
		response.setHeader("Location", uri.toASCIIString());
	}
}

package mg.restgen.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
		// allow all due to testing purposes, comment all (*) and allow only
		// target port for localhost.

		// localhost:3000 -> nodejs default server port.
		// response.getHeaders()
		// 		.add("Access-Control-Allow-Origin", "http://localhost:3000");
		response.getHeaders()
				.add("Access-Control-Allow-Origin", "*");
		response.getHeaders()
				.add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
		response.getHeaders()
				.add("Access-Control-Allow-Credentials", "true");
		response.getHeaders()
				.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
	}
}

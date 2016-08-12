package edu.emory.bmi.aiw.i2b2export.resource;

/*
 * #%L
 * i2b2 Export Service
 * %%
 * Copyright (C) 2013 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import edu.emory.bmi.aiw.i2b2export.comm.DeleteRequest;
import edu.emory.bmi.aiw.i2b2export.comm.LoadRequest;
import edu.emory.bmi.aiw.i2b2export.comm.OutputConfiguration;
import edu.emory.bmi.aiw.i2b2export.comm.OutputConfigurationSummary;
import edu.emory.bmi.aiw.i2b2export.comm.SaveRequest;
import edu.emory.bmi.aiw.i2b2export.dao.OutputConfigurationDao;
import edu.emory.bmi.aiw.i2b2export.entity.OutputConfigurationEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.eurekaclinical.i2b2.client.I2b2UserAuthenticator;
import org.eurekaclinical.i2b2.client.comm.I2b2AuthMetadata;
import org.eurekaclinical.i2b2.client.xml.I2b2XmlException;
import org.eurekaclinical.standardapis.exception.HttpStatusException;

/**
 * A Jersey resource for handling requests relating to output configurations.
 *
 * @author Michel Mansour
 * @since 1.0
 */
@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OutputConfigurationResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutputConfigurationResource.class);

	private final I2b2UserAuthenticator userAuthenticator;
	private final OutputConfigurationDao dao;

	/**
	 * Default constructor.
	 *
	 * @param dao the output configuration DAO
	 * @param userAuthenticator the user authenticator
	 */
	@Inject
	public OutputConfigurationResource(OutputConfigurationDao dao,
									I2b2UserAuthenticator userAuthenticator) {
		this.userAuthenticator = userAuthenticator;
		this.dao = dao;
	}

	/**
	 * Saves the output configuration as specified in the given request.
	 *
	 * @param request the create request, containing the configuration to create
	 *                along with the i2b2 authentication tokens
	 * @return a status code indicating success or failure
	 *
	 */
	@POST
	@Path("/save")
	public Response saveConfiguration(SaveRequest request) {
		LOGGER.info("Received request to save configuration for user: {}", request.getAuthMetadata().getUsername());

		try {
			if (this.userAuthenticator.authenticateUser(request.getAuthMetadata())) {
				String username = request.getAuthMetadata().getUsername();
				OutputConfigurationEntity config = this.dao
						.getByUsernameAndConfigName(username,
								request.getOutputConfiguration().getName());
				if (config != null) {
					if (config.getUsername().equals(username)) {
						LOGGER.info("Configuration with name: {} already exists for user: {}. Updating existing configuration.",
								config.getName(), config.getUsername());
						OutputConfiguration outputConfiguration = request.getOutputConfiguration();
						outputConfiguration.setId(config.getId());
						outputConfiguration.setUsername(username);
						this.dao.update(outputConfiguration.toEntity());
					} else {
						LOGGER.warn("Usernames do not match: request username: {}, existing configuration username: {}",
								request.getAuthMetadata().getUsername(), config.getUsername());
						return Response.status(Response.Status.UNAUTHORIZED)
								.build();
					}
				} else {
					LOGGER.info("Creating new configuration for user: {} with name: {}", request.getAuthMetadata().getUsername(),
							request.getOutputConfiguration().getName());
					request.getOutputConfiguration().setUsername(username);
					this.dao.create(request.getOutputConfiguration().toEntity());
				}

				return Response.ok().build();
			} else {
				LOGGER.warn("User not authenticated: {}", request.getAuthMetadata().getUsername());
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
		} catch (I2b2XmlException e) {
			logError(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Loads the output configuration specified in the given request.
	 *
	 * @param request contains the information needed to complete the load
	 *                operation, including the configuration ID and the
	 *                i2b2 authentication tokens
	 * @return the output configuration or a status code indicating failure
	 */
	@POST
	@Path("/load")
	@Transactional
	public OutputConfiguration loadConfiguration(LoadRequest request) {
		LOGGER.info("Received request to load configuration for user: {} with id: {}",
				request.getAuthMetadata().getUsername(), request.getOutputConfigurationId());

		try {
			if (this.userAuthenticator.authenticateUser(request.getAuthMetadata())) {
				OutputConfigurationEntity config = this.dao
						.getById(request.getOutputConfigurationId());
				if (config != null) {
					if (config.getUsername().equals(request.getAuthMetadata().getUsername())) {
						LOGGER.info("Found configuration with name: {}", config.getName());
						return config.toDTO();
					} else {
						LOGGER.warn("Found configuration with name: {}, but usernames do not match: request user: {}, config user: {}",
								new String[]{config.getName(), request.getAuthMetadata().getUsername(), config.getUsername()});
						throw new HttpStatusException(Response.Status.UNAUTHORIZED);
					}
				} else {
					LOGGER.warn("Configuration not found with id: {}", request.getOutputConfigurationId());
					throw new HttpStatusException(Response.Status.NOT_FOUND);
				}
			} else {
				LOGGER.warn("User not authenticated: {}", request.getAuthMetadata().getUsername());
				throw new HttpStatusException(Response.Status.UNAUTHORIZED);
			}
		} catch (I2b2XmlException e) {
			logError(e);
			throw new HttpStatusException(Response.Status.INTERNAL_SERVER_ERROR, e);
		}
	}

	/**
	 * Retrieves the names of all of the specified user's output
	 * configurations.
	 *
	 * @param authMetadata the i2b2 authentication tokens identifying the user
	 * @return a list of configuration names or a status indicating failure
	 */
	@POST
	@Path("/getAll")
	public Response getConfigurationsByUser(I2b2AuthMetadata authMetadata) {
		LOGGER.info("Received request to retrieve all configurations for user: {}", authMetadata.getUsername());

		try {
			if (this.userAuthenticator.authenticateUser(authMetadata)) {
				List<OutputConfigurationSummary> result = new ArrayList<>();
				List<OutputConfigurationEntity> configs = this.dao.getAllByUsername(authMetadata.getUsername());
				for (OutputConfigurationEntity config : configs) {
					if (config.getUsername().equals(authMetadata.getUsername())) {
						result.add(new OutputConfigurationSummary(config.getId(),
								config.getName()));
					} else {
						LOGGER.warn("Skipping configuration with id: {} because configuration username does not match" +
								" request username: request user: {}, config user: {}",
								new String[]{config.getId().toString(), authMetadata.getUsername(), config.getUsername()});
					}
				}

				return Response.ok().entity(result).build();
			} else {
				LOGGER.warn("User not authenticated: {}", authMetadata.getUsername());
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
		} catch (I2b2XmlException e) {
			logError(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Deletes the output configurations specified in the given request.
	 *
	 * @param request contains the information needed to complete the remove
                operation, including the configuration ID and the
                i2b2 authentication tokens
	 * @return a status code indicating success or failure
	 */
	@POST
	@Path("/delete")
	public Response removeConfiguration(DeleteRequest request) {
		LOGGER.info("Received request to delete configuration with id: {}", request.getOutputConfigurationId());

		try {
			if (this.userAuthenticator.authenticateUser(request.getAuthMetadata())) {
				OutputConfigurationEntity config = this.dao.getById(request.getOutputConfigurationId());
				if (config != null) {
					LOGGER.debug("Found configuration with id: {}", config.getId());
					if (config.getUsername().equals(request.getAuthMetadata().getUsername())) {
						this.dao.remove(config);
						return Response.ok().build();
					} else {
						LOGGER.warn("Not deleting configuration with id: {} because request username does not match" +
								" configuration username: request user: {}, configuration user: {}",
								new String[]{request.getOutputConfigurationId().toString(),
										request.getAuthMetadata().getUsername(), config.getUsername()});
						return Response.status(Response.Status.UNAUTHORIZED).build();
					}
				}
				LOGGER.warn("Configuration with id: {} not found", request.getOutputConfigurationId());
				return Response.status(Response.Status.NOT_FOUND).build();
			} else {
				LOGGER.warn("User not authenticated: {}", request.getAuthMetadata().getUsername());
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
		} catch (I2b2XmlException e) {
			logError(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	private static void logError(Throwable e) {
		LOGGER.error("Exception thrown: {}", e);
	}
}

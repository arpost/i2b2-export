package edu.emory.bmi.aiw.i2b2export.config;

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

import com.google.inject.persist.PersistFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Jersey servlet configuration.
 *
 * @author Michel Mansour
 * @since 1.0
 */
public class ServletConfigModule extends JerseyServletModule {

	@Override
	protected void configureServlets() {
		filter("/rest/*").through(PersistFilter.class);
		super.configureServlets();

		Map<String, String> params = new HashMap<>();
		params.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
		params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
				"edu.emory.bmi.aiw.i2b2export.resource");
		serve("/rest/*").with(GuiceContainer.class, params);
	}
}

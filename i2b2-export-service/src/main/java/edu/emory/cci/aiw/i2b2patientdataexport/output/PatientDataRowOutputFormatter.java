package edu.emory.cci.aiw.i2b2patientdataexport.output;

/*
 * #%L
 * i2b2 Patient Data Export Service
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

import edu.emory.cci.aiw.i2b2patientdataexport.entity.I2b2Concept;
import edu.emory.cci.aiw.i2b2patientdataexport.entity.OutputConfiguration;
import edu.emory.cci.aiw.i2b2patientdataexport.i2b2.pdo.Event;
import edu.emory.cci.aiw.i2b2patientdataexport.i2b2.pdo.Observation;
import edu.emory.cci.aiw.i2b2patientdataexport.i2b2.pdo.Patient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class PatientDataRowOutputFormatter extends DataRowOutputFormatter {
	private final Patient patient;

	public PatientDataRowOutputFormatter(OutputConfiguration config, Patient patient) {
		super(config);
		this.patient = patient;
	}

	@Override
	protected List<String> rowPrefix() {
		return Collections.singletonList(patient.getPatientId());
	}

	@Override
	protected Collection<Observation> matchingObservations(I2b2Concept i2b2Concept) {
		Collection<Observation> result = new ArrayList<Observation>();

		for (Event e : patient.getEvents()) {
			for (Observation o : e.getObservations()) {
				if (o.getConceptPath().equals(i2b2Concept.getI2b2Key())) {
					result.add(o);
				}
			}
		}

		return result;
	}
}
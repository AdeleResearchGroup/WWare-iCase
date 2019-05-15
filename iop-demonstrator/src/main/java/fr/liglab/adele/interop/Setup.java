/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.interop;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;

import fr.liglab.adele.interop.demonstrator.applications.ApplicationManager;
import fr.liglab.adele.interop.demonstrator.applications.temperature.AutonomicManager;
import fr.liglab.adele.interop.demonstrator.applications.temperature.TemperatureOpenControllerApplication;
import fr.liglab.adele.interop.iop.publisher.LocatedObjectPublisher;
import fr.liglab.adele.interop.time.series.MeasurementStorage;
import fr.liglab.adele.time.series.influxdb.InfluxSeriesDatabase;

import static org.apache.felix.ipojo.configuration.Instance.instance;


@Configuration
public class Setup {

    Instance manager = instance().named("Interop-ApplicationManager")
            .of(ApplicationManager.class.getCanonicalName());

    Instance specialManager = instance().named("Interop-TemperatureApplicationManager")
            .of(AutonomicManager.class.getCanonicalName());

	Instance icasaPublisher = instance().named("iCasaPublisher")
            .of(LocatedObjectPublisher.class.getCanonicalName());

	Instance measurementStrorage = instance().named("MeasurementStorage")
            .of(MeasurementStorage.class.getCanonicalName());

	Instance measurementDatabase = instance().named("MeasurementDatabase")
            .of(InfluxSeriesDatabase.class.getCanonicalName())
            .with("name").setto(MeasurementStorage.DATABASE_NAME)
            .with("server").setto("http://localhost:8086")
            .with("user").setto("root")
            .with("password").setto("root");

	Instance learningtDatabase = instance().named("LearningDatabase")
            .of(InfluxSeriesDatabase.class.getCanonicalName())
            .with("name").setto(TemperatureOpenControllerApplication.DATABASE_NAME)
            .with("server").setto("http://localhost:8086")
            .with("user").setto("root")
            .with("password").setto("root");


}

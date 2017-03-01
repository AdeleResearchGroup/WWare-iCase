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
package fr.liglab.adele.iop.device;

import org.apache.felix.ipojo.configuration.Configuration;
import org.apache.felix.ipojo.configuration.Instance;
import org.ow2.chameleon.fuchsia.core.FuchsiaConstants;
import org.ow2.chameleon.fuchsia.core.component.ExportationLinker;
import org.ow2.chameleon.fuchsia.core.component.ImportationLinker;
import static org.apache.felix.ipojo.configuration.Instance.instance;


@Configuration
public class FuchsiaSetup {

	Instance fileBasedDiscovery = instance()
	            .of("org.ow2.chameleon.fuchsia.discovery.filebased.FileBasedDiscoveryImport");
	 
    Instance iopControllerImporter = instance().named("iopControllerImporter")
            .of("fr.liglab.adele.iop.device.importer.ControllerImporter");

    Instance iopDeviceImporter = instance().named("iopServiceImporter")
            .of("fr.liglab.adele.iop.device.importer.ServiceImporter");

    Instance iopDeviceExporter = instance().named("iopServiceExporter")
            .of("fr.liglab.adele.iop.device.exporter.ServiceExporter");

    Instance iopImporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(scope=generic)(protocol=iop)(SELF_ID=*)(SELF_LOCATION=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=iopControllerImporter)");

    Instance iopDeviceImporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_IMPORTATION_LINKER_FACTORY_NAME)
            .with(ImportationLinker.FILTER_IMPORTDECLARATION_PROPERTY).setto("(&(scope=generic)(iop.service.description=*))")
            .with(ImportationLinker.FILTER_IMPORTERSERVICE_PROPERTY).setto("(instance.name=iopServiceImporter)");

    Instance iopDeviceExporterLinker = instance()
            .of(FuchsiaConstants.DEFAULT_EXPORTATION_LINKER_FACTORY_NAME)
            .with(ExportationLinker.FILTER_EXPORTDECLARATION_PROPERTY).setto("(&(scope=generic)(iop.exported.service.id=*)(iop.exported.service.capabilities=*))")
            .with(ExportationLinker.FILTER_EXPORTERSERVICE_PROPERTY).setto("(instance.name=iopServiceExporter)");
    
}

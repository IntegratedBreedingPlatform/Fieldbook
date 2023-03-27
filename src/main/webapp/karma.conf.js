// Karma configuration

// Get the Chromium executable bundled in Puppeteer.
process.env.CHROME_BIN = require('puppeteer').executablePath();

module.exports = function (config) {
	'use strict';
	config.set({

		// base path that will be used to resolve all patterns (eg. files, exclude)
		basePath: '',

		// frameworks to use
		// available frameworks: https://npmjs.org/browse/keyword/karma-adapter
		frameworks: ['jasmine'],

		// list of files / patterns to load in the browser
		files: [

			'WEB-INF/static/js/lib/jquery/jquery-1.11.3.js',
			'WEB-INF/static/js/angular-tests/mocks/globals-mock.js',

			// AngularJS plugins and dependencies
			'WEB-INF/static/js/lib/angular/angular.js',
			'WEB-INF/static/js/lib/angular/angular-messages.min.js',
			'WEB-INF/static/js/lib/angular/angular-local-storage.min.js',
			'WEB-INF/static/js/lib/angular/angular-resource.min.js',
			'WEB-INF/static/js/lib/angular/angular-sanitize.js',
			'WEB-INF/static/js/lib/angular/angular-select2.js',
			'WEB-INF/static/js/lib/angular/ui-router-core.min.js',
			'WEB-INF/static/js/lib/angular/ui-router-dsr.min.js',
			'WEB-INF/static/js/lib/angular/ui-router-sticky-states.js',
			'WEB-INF/static/js/lib/angular/ui-router-angularjs.min.js',
			'WEB-INF/static/js/lib/angular/angular-block-ui.min.js',
			'WEB-INF/static/js/lib/angular/ng-lodash.min.js',
			'WEB-INF/static/js/lib/angular/sanitize.js',
			'WEB-INF/static/js/lib/angular/select.js',
			'WEB-INF/static/js/lib/angular/sortable.js',
			'WEB-INF/static/js/lib/angular/ui-bootstrap-tpls-2.5.0.min.js',
			'WEB-INF/static/js/lib/angular/angular-translate.min.js',
			'WEB-INF/static/js/lib/datatable/jquery.dataTables.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.buttons.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.fixedcolumns.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.colreorder.js',
			'WEB-INF/static/js/lib/datatable/buttons.colVis.min.js',
			'WEB-INF/static/js/lib/datatable/dataTables.bootstrap.js',
			'WEB-INF/static/js/lib/datatable/dataTables.buttons.min.js',
			'WEB-INF/static/js/lib/datatable/dataTables.colReorder.js',
			'WEB-INF/static/js/lib/datatable/dataTables.colResize.js',
			'WEB-INF/static/js/lib/datatable/dataTables.colVis.js',
			'WEB-INF/static/js/lib/datatable/dataTables.fixedColumns.js',
			'WEB-INF/static/js/lib/datatable/dataTables.scroller.js',
			'WEB-INF/static/js/lib/jquery/jquery.contextMenu-2.0.min.js',

			'WEB-INF/static/js/angular/angular-local-storage.min.js',
			'WEB-INF/static/js/angular/bmsAuth.js',
			'WEB-INF/static/js/lib/angular/xlsx.full.min.js',
			'node_modules/babel-polyfill/dist/polyfill.js',


			// Modules to test
			'WEB-INF/static/js/angular/angular-utilities.js',
			'WEB-INF/static/js/importObservations/variateGroupImporter.js',
			'WEB-INF/static/js/angular/fieldbook-utils.js',
			'WEB-INF/static/js/trialmanager/manageTrial.js',
			'WEB-INF/static/js/auth/auth.module.js',
			'WEB-INF/static/js/trialmanager/trial-settings-manager.js',
			'WEB-INF/static/js/trialmanager/selectEnvironmentModal.js',
			'WEB-INF/static/js/trialmanager/studyStateService.js',
			'WEB-INF/static/js/trialmanager/studyInstanceService.js',
			'WEB-INF/static/js/trialmanager/studyEntryService.js',
			'WEB-INF/static/js/trialmanager/derivedVariable.js',
			'WEB-INF/static/js/trialmanager/trial-data-manager.js',
			'WEB-INF/static/js/trialmanager/saveSampleList.js',
			'WEB-INF/static/js/trialmanager/exportSampleList.js',
			'WEB-INF/static/js/trialmanager/trialSettings.js',
			'WEB-INF/static/js/trialmanager/treatment.js',
			'WEB-INF/static/js/trialmanager/environments.js',
			'WEB-INF/static/js/trialmanager/experimentalDesign.js',
			'WEB-INF/static/js/trialmanager/subobservations/subObservationSet.js',
			'WEB-INF/static/js/trialmanager/germplasm-selector/germplasm-selector-modal.js',
			'WEB-INF/static/js/trialmanager/datasetService.js',
			'WEB-INF/static/js/trialmanager/exportStudy.js',
			'WEB-INF/static/js/trialmanager/datasetOptionModal.js',
			'WEB-INF/static/js/design-import/design-import-main.js',
			'WEB-INF/static/js/trialmanager/importStudy.js',
			'WEB-INF/static/js/trialmanager/createSample.js',
			'WEB-INF/static/js/trialmanager/inventory/planting-preparation/planting-preparation-modal.js',
			'WEB-INF/static/js/trialmanager/germplasm-study-source/germplasmStudySource.js',
			'WEB-INF/static/js/trialmanager/germplasm-study-source/germplasmStudySourceService.js',
			'WEB-INF/static/js/trialmanager/sample-genotype/sampleGenotypeTab.js',
			'WEB-INF/static/js/trialmanager/sample-genotype/sampleGenotypeService.js',
			'WEB-INF/static/js/trialmanager/observations/change-plot-entry-modal.js',
			'WEB-INF/static/js/trialmanager/observations/change-plot-entry-service.js',
			'WEB-INF/static/js/trialmanager/location/locationService.js',
			'WEB-INF/static/js/trialmanager/method/methodService.js',
			'WEB-INF/static/js/trialmanager/germplasm/germplasm-details-modal/germplasm-details-module.js',

			// unit test files
			'WEB-INF/static/js/angular-tests/lib/angular-mocks.js',
			'WEB-INF/static/js/angular-tests/trialmanager/*.spec.js'
		],
		exclude: [],
		preprocessors: {},
		reporters: ['dots'],
		port: 9876,
		colors: true,
		browserConsoleLogOptions: {
			level: config.LOG_INFO,
			terminal: true
		},
		autoWatch: false,
		browsers: ['ChromeHeadlessNoSandbox'],
		singleRun: true,
		customLaunchers: {
			ChromeHeadlessNoSandbox: {
				base: 'ChromeHeadless',
				flags: ['--no-sandbox']
			}
		}
	});
};

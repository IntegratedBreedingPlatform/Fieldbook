var variableDetailsHeader = /*[[#{variable.details.heading}]]*/ '',
	startDateRequiredEarlierError = /*[[#{start.end.date.validation.start.date.required.earlier}]]*/ '',
	startDateRequiredError = /*[[#{start.end.date.validation.start.date.required}]]*/ '',
	addNurseryLevelSettings = /*[[#{study.managesettings.add.study.level.settings}]]*/ '',
	addPlotLevelSettings = /*[[#{study.managesettings.add.plot.level.settings}]]*/ '',
	addBaselineTraits = /*[[#{study.managesettings.add.baseline.traits}]]*/ '',
	addSelectionVariates = /*[[#{study.managesettings.add.selection.variates}]]*/ '',
	addStudyConditions = /*[[#{study.managesettings.add.study.conditions}]]*/ '',
	addStudyEnvironmentTraits = /*[[#{study.managesettings.add.study.environment.variable}]]*/ '',
	addTreatmentFactors = /*[[#{study.managesettings.treatment.factors.add}]]*/ '',
	reminderNursery = /*[[#{study.reminder.add.study.settings}]]*/ '',
	reminderPlot = /*[[#{study.reminder.add.plot.settings}]]*/ '',
	reminderTraits = /*[[#{study.reminder.add.trait.settings}]]*/ '',
	reminderSelectionVariates = /*[[#{study.reminder.add.selection.variates}]]*/ '',
	reminderNurseryCondtions = /*[[#{study.reminder.add.study.conditions}]]*/ '',
	reminderTrialEnvironment = /*[[#{study.reminder.add.study.environment.settings}]]*/ '',
	reminderTreatmentFactors = /*[[#{study.reminder.add.treatment.factors}]]*/ '',
	germplasmSelectError = /*[[#{seed.inventory.select.germplasm.error}]]*/ '',
	locationRequired = /*[[#{seed.inventory.location.required}]]*/ '',
	scaleRequired = /*[[#{seed.inventory.scale.required}]]*/ '',
	inventoryAmountRequired = /*[[#{seed.inventory.amount.required.error}]]*/ '',
	inventoryAmountPositiveRequired = /*[[#{seed.inventory.amount.required.error}]]*/ '',
	commentLimitError = /*[[#{seed.inventory.comment.limit.error}]]*/ '',
	programNurseriesText = /*[[#{program.studies}]]*/ '',
	noAssignedMethodWarning = /*[[#{warning.advancing.nursery.skipped.no.breeding.method}]]*/ '',
	invalidFolderNameCharacterMessage = /*[[#{study.addfolder.error.invalidcharacters}]]*/'',
	deleteConfirmation = /*[[#{browse.study.delete.folder.name.confirmation}]]*/ '',
	deleteNurseryConfirmation = /*[[#{browse.study.delete.study.name.confirmation}]]*/ '',
	deleteStudyConfirmation = /*[[#{browse.study.delete.study.name.confirmation}]]*/ '',
	deleteFolderTitle = /*[[#{browse.study.delete.folder.name}]]*/ '',
	deleteNurseryTitle = /*[[#{browse.nursery.delete.nursery.name}]]*/ '',
	deleteStudyTitle = /*[[#{browse.study.delete.study.name}]]*/ '',
	deleteFolderHasTrial = /*[[#{browse.study.delete.folder.has.study.children}]]*/ '',
	folderNameRequiredMessage = /*[[#{browse.study.add.folder.name.blank}]]*/ '',
	renameFolderHeader = /*[[#{browse.study.rename.folder.name}]]*/ '',
	seasrchErrorMessage = /*[[#{ontology.search.error.no.result}]]*/ '',
	variableDetailHeader = /*[[#{ontology.variable.details}]]*/'',
	noVariableNameError = /*[[#{manage.settings.no.variable.name.error}]]*/ '',
	noTreatmentFactorPairError = /*[[#{manage.settings.no.treatment.factor.pair.missing}]]*/ '',
	noTreatmentFactorPairNameError = /*[[#{manage.settings.no.treatment.factor.pair.name.missing}]]*/ '',
	germplasmStartingEntry = /*[[#{import.germplasm.germplasm.specified.starting.entry}]]*/ '',
	germplasmAddedAsCheck = /*[[#{import.germplasm.germplasm.added.as.check}]]*/ '',
	startingEntryErr = /*[[#{import.germplasm.starting.entry.in.check.list}]]*/ '',
	sameListErr = /*[[#{import.germplasm.same.list.primary.check}]]*/ '',
	startingEntryRequired = /*[[#{import.germplasm.starting.entry.required}]]*/ '',
	errorTheVariable = /*[[#{error.add.the.variable}]]*/ '',
	errorTheVariableNurseryUnique = /*[[#{error.add.the.variable.should.be.unique.study}]]*/ '',
	checkIntervalGreaterThanZeroError = /*[[#{germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero}]]*/ '',
	intervalWholeNumberError = /*[[#{germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number}]]*/ '',
	startIndexWholeNumberError = /*[[#{germplasm.list.start.index.whole.number.error}]]*/ '',
	startIndexLessGermplasmError = /*[[#{germplasm.list.start.index.less.than.germplasm.error}]]*/ '',
	selectedCheckError = /*[[#{germplasm.list.check.type.error}]]*/ '',
	importRequiredFieldsError = /*[[#{study.import.excel.fill.up.required.fields}]]*/ '',
	errorMsgHeader = /*[[#{common.error.header}]]*/ '',
	warningMsgHeader = /*[[#{common.warning.header}]]*/ '',
	successMsgHeader = /*[[#{common.form.success.text}]]*/ '',
	invalidInputMsgHeader = /*[[#{common.error.invalid.input.header}]]*/ '',
	studyProgramFolderRequired = /*[[#{add.rename.folder.local.folder.required}]]*/ '',
	addFolderSuccessful = /*[[#{add.folder.successful}]]*/ '',
	renameItemSuccessful = /*[[#{rename.item.successful}]]*/ '',
	renameFolderSuccessful = /*[[#{rename.folder.successful}]]*/ '',
	deleteItemSuccessful = /*[[#{delete.item.successful}]]*/ '',
	deleteFolderSuccessful = /*[[#{delete.folder.successful}]]*/ '',
	deleteNurserySuccessful = /*[[#{delete.study.successful}]]*/ '',
	deleteStudySuccessful = /*[[#{delete.study.successful}]]*/ '',
	notificationHeader = /*[[#{common.form.notification.text}]]*/ '',
	renameGermplasmInvalidFolderMessage = /*[[#{study.germplasm.list.tree.invalid.public}]]*/ '',
	invalidNodeGermplasmTreeMessage = /*[[#{study.germplasm.list.tree.invalid.node}]]*/ '',
	addGermplasmInvalidFolderMessage = /*[[#{study.germplasm.list.tree.invalid.public}]]*/ '',
	importSuccessReminderToSaveMessage = /*[[#{import.success.reminder.to.save}]]*/ '',
	importSuccessOverwriteDataWarningToSaveMessage = /*[[#{import.success.overwrite.data.warning}]]*/ '',
	importSaveDataWarningMessage = /*[[#{study.import.save.data.warning}]]*/ '',
	ajaxGenericErrorMsg = /*[[#{ajax.generic.exception.error.message}]]*/ '',
	invalidStartingNumberErrorMsg = /*[[#{study.import.crosses.error.invalid.starting.number}]]*/ '',
	needSaveImportDataError = /*[[#{study.advance.error.need.save.import.data}]]*/ '',
	importDateRequired = /*[[#{study.import.date.required}]]*/ '',
	importLocationRequired = /*[[#{study.import.location.required}]]*/ '',
	importMethodRequired = /*[[#{study.import.method.required}]]*/ '',
	checkTypeCurrentlyUseError = /*[[#{germplasm.list.check.type.delete.currently.use.error}]]*/ '',
	headerMsg1 = /*[[#{open.germplasm.header}]]*/ '',
	headerMsg2 = /*[[#{open.germplasm.gid}]]*/ '',
	idNameVariables = /*[[${T(com.efficio.fieldbook.web.util.AppConstants).ID_NAME_COMBINATION.getString()}]]*/ '',
	idCodeNameCombination = /*[[${T(com.efficio.fieldbook.web.util.AppConstants).ID_CODE_NAME_COMBINATION_STUDY.getString()+","+T(com.efficio.fieldbook.web.util.AppConstants).ID_CODE_NAME_COMBINATION_VARIATE.getString()}]]*/ '',
	saveSuccessMessage = /*[[#{common.form.save.successful.text}]]*/ '',
	outOfSyncWarningMessage = /*[[#{study.out.of.sync.values.warning}]]*/ '',
	unpairedTreatmentFactor = /*[[#{treatment.factor.invalid.unpaired}]]*/ '',
	invalidTreatmentFactorPair = /*[[#{treatment.factor.invalid.usedpair}]]*/ '',
	unsavedTreatmentFactor = /*[[#{treatment.factor.invalid.unsaved}]]*/ '',
	environmentModalConfirmationText = /*[[#{study.change.environment.confirmation}]]*/ '',
	observationVariableDeleteConfirmationText  = /*[[#{study.edit.study.confirm.delete.variable}]]*/ '',
	removeVariableDependencyConfirmationText = /*[[#{study.edit.study.confirm.delete.dependency.variate}]]*/ '',
	fieldmapRequireOneNursery = /*[[#{fieldmap.please.choose.at.least.one.study}]]*/ '',
	fieldmapRequireOneTrial = /*[[#{fieldmap.please.choose.at.least.one.study}]]*/ '',
	trialAlreadyAdded = /*[[#{fieldmap.study.already.added}]]*/ '',
	trialNoObservations = /*[[#{fieldmap.study.no.observations}]]*/ '',
	studiesSelectedFieldPlan = /*[[#{fieldmap.study.selected.for.field.plan}]]*/ '',
	additionalStudiesForFieldPlan = /*[[#{fieldmap.study.additional.for.field.plan}]]*/ '',
	noStudyGermplasmList = /*[[#{study.import.no.germplasm.list}]]*/ '',
	errorAdvancingStudyMutipleTimes = /*[[#{error.advancing.study.multiple.times}]]*/ '',
	importDataWarningNotification = /*[[#{import.data.warning.notification}]]*/ '',
	commonErrorDateFormat = /*[[#{common.error.date.format}]]*/ '',
	commonErrorInvalidYear = /*[[#{common.error.invalid.year}]]*/ '',
	studyInstanceRequired = /*[[#{fieldmap.no.selected.study.instance}]]*/ '',
	standardVariableMaxLength = /*[[${T(com.efficio.fieldbook.web.util.AppConstants).STANDARD_VARIABLE_NAME_LIMIT.getInt()}]]*/ '',
	variableNoValidValueNotification = /*[[#{common.warning.variable.no.valid.values}]]*/ '',
	redirectErrorMessage = /*[[${redirectErrorMessage}]]*/ '',
	saveListSuccessfullyMessage = /*[[#{germplasm.save.list.success}]]*/ '',
	saveSampleListSuccessfullyMessage = /*[[#{sampleList.save.list.success}]]*/ '',
	crossesWarningMessage = /*[[#{crosses.truncated}]]*/ '',
	expDesignMsgs = {
		0: /*[[#{experiment.design.invalid.generic.error}]]*/ '',
		1: /*[[#{experiment.design.rows.per.replication.should.be.a.number}]]*/ '',
		2: /*[[#{experiment.design.cols.per.replication.should.be.a.number}]]*/ '',
		3: /*[[#{experiment.design.replication.count.should.be.a.number}]]*/ '',
		4: /*[[#{experiment.design.replication.count.rcbd.error}]]*/ '',
		5: /*[[#{experiment.design.replication.count.resolvable.error}]]*/ '',
		6: /*[[#{experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size}]]*/ '',
		7: /*[[#{experiment.design.block.size.should.be.a.number}]]*/ '',
		8: /*[[#{experiment.design.block.size.should.be.a.greater.than.1}]]*/ '',
		9: /*[[#{experiment.design.generate.no.germplasm}]]*/ '',
		10: /*[[#{experiment.design.generate.generic.error}]]*/ '',
		11: /*[[#{experiment.design.nblatin.should.not.be.greater.than.block.level}]]*/ '',
		12: /*[[#{experiment.design.replating.groups.not.equal.to.replicates}]]*/ '',
		13: /*[[#{experiment.design.block.size.not.a.factor.of.treatment.size}]]*/ '',
		14: /*[[#{experiment.design.nrlatin.should.be.less.than.rows.per.replication}]]*/ '',
		15: /*[[#{experiment.design.nrlatin.should.not.be.greater.than.the.replication.count}]]*/ '',
		16: /*[[#{experiment.design.nclatin.should.not.be.greater.than.the.replication.count}]]*/ '',
		17: /*[[#{experiment.design.nclatin.should.be.less.than.cols.per.replication}]]*/ '',
		18: /*[[#{experiment.design.no.treatment}]]*/ '',
		19: /*[[#{experiment.design.block.size.should.be.divisible}]]*/ '',
		20: /*[[#{experiment.design.block.size.contiguous}]]*/ '',
		21: /*[[#{experiment.design.select.field.arrangement}]]*/ '',
		22: /*[[#{experiment.design.replatinGroups.invalid}]]*/ '',
		23: /*[[#{experiment.design.nblatin.should.not.be.greater.than.the.replication.count}]]*/ '',
		24: invalidTreatmentFactorPair,
		25: unpairedTreatmentFactor,
		26: /*[[#{experiment.design.no.germplasm.list}]]*/'',
		27: /*[[#{experiment.design.nblatin.should.be.greater.than.zero}]]*/ '',
		28: /*[[#{experiment.design.no.of.entries.does.not.match}]]*/ '',
		29: /*[[#{germplasm.list.spacing.less.than.germplasm.error}]]*/'',
		30: /*[[#{germplasm.list.start.index.less.than.germplasm.error}]]*/'',
		31: /*[[#{germplasm.list.number.of.rows.between.insertion.should.be.greater.than.zero}]]*/'',
		32: /*[[#{germplasm.list.starting.index.should.be.greater.than.zero}]]*/'',
		33: /*[[#{germplasm.list.all.entries.can.not.be.checks}]]*/''
	},
	plotNoTermId = /*[[${T(org.generationcp.middleware.domain.oms.TermId).PLOT_NO.getId()}]]*/ '',
	gidTermId = /*[[${T(org.generationcp.middleware.domain.oms.TermId).GID.getId()}]]*/ '',
	entryNoTermId = /*[[${T(org.generationcp.middleware.domain.oms.TermId).ENTRY_NO.getId()}]]*/ '',
	desigTermId = /*[[${T(org.generationcp.middleware.domain.oms.TermId).DESIG.getId()}]]*/ '',
	requiredGermplasmColumnsMessage = /*[[#{germplasm.list.required.germplasm.hidden}]]*/ '',
	reviewOutOfBoundsDataActionRequiredError = /*[[#{review.out.of.bounds.data.action.required}]]*/ '',
	crossingImportErrorHeader = /*[[#{study.import.crosses.error.header}]]*/ '',
	crossingExportErrorHeader = /*[[#{study.export.crosses.error.header}]]*/ '',
	designImportErrorHeader = /*[[#{study.import.design.error.header}]]*/ '',
	invalidImportedFile = /*[[#{common.error.invalid.file}]]*/ '',
	listParentFolderRequired = /*[[#{germplasm.save.list.folder.error}]]*/ '',
	listNameRequired = /*[[#{germplasm.save.list.name.error}]]*/ '',
	listDateRequired = /*[[#{germplasm.save.list.date.error}]]*/ '',
	listTypeRequired = /*[[#{germplasm.save.list.type.error}]]*/ '',
	listNameDuplicateError = /*[[#{germplasm.save.list.name.unique.error}]]*/ '',
	crossingSaveNurseryBeforeImport = /*[[#{study.import.crosses.error.save.study.before.import}]]*/ '',
	crossingSettingsSaved = /*[[#{study.import.crosses.settings.saved}]]*/ '',
	crossingSettingsDeleted = /*[[#{study.import.crosses.settings.deleted}]]*/ '',
	crossingSettingsDeleteTitle = /*[[#{study.import.crosses.settings.delete.title}]]*/ '',
	crossingSettingsDeleteConfirm = /*[[#{study.import.crosses.settings.delete.confirm}]]*/ '',
	crossingSettingsDeleteFailed = /*[[#{study.import.crosses.settings.delete.fail}]]*/ '',
	stockIdGenerationPrefixError = /*[[#{stock.generate.id.breeder.identifier.error.numbers.found}]]*/ '',
	addEnvironmentsImportDesignWarning = /*[[#{study.add.environments.import.design.warning}]]*/ '',
	addEnvironmentsImportDesignMessage = /*[[#{study.add.environments.import.design.message}]]*/ '';
$.environmentMessages = {};
$.environmentMessages.environmentHasDataThatWillBeLost = /*[[#{environment.has.plot.data}]]*/ '';
$.environmentMessages.deleteEnvironmentNoData = /*[[#{environment.delete.no.plot.data}]]*/ '';
$.experimentDesignMessages = {};
$.experimentDesignMessages.experimentDesignGeneratedSuccessfully = /*[[#{experiment.design.generated.successfully}]]*/ '';
$.fieldbookMessages = {};
$.fieldbookMessages.studyManageSettingsManageLocation = /*[[#{study.managesettings.manage.location}]]*/ '';
$.fieldbookMessages.errorNoFileSelectedForImport = /*[[#{error.no.file.selected.for.import}]]*/ '';
$.fieldbookMessages.errorImportCrossesSettingsFailed = /*[[#{error.import.crosses.settings.failed}]]*/ '';
$.fieldbookMessages.errorImportMethodRequired = /*[[#{study.import.method.required}]]*/ '';
$.fieldbookMessages.errorNoNextNameInSequence = /*[[#{error.no.next.name.in.sequence}]]*/ '';
$.fieldbookMessages.errorImportFailed = /*[[#{common.import.failed}]]*/ '';
$.fieldbookMessages.errorNoNamePrefix = /*[[#{error.no.name.prefix}]]*/ '';
$.fieldbookMessages.errorNoParentageDesignationSeparator = /*[[#{error.no.parentage.designation.separator}]]*/ '';
$.fieldbookMessages.advanceListUnableToGenerateWarningMessage = /*[[#{study.advance.study.must.contain.EXPT_DESIGN.variable}]]*/ '';
$.fieldbookMessages.determinedFromParentalLines = /*[[#{crosses.method.determined.from.parental.lines}]]*/ '';
$.fieldbookMessages.errorMethodMissing = /*[[#{study.import.method.missing}]]*/ '';
$.fieldbookMessages.measurementWarningNeedGenExpDesign = /*[[#{experiment.design.observations.warning}]]*/ '',
	$.fieldbookMessages.measurementsTraitsChangeWarning = /*[[#{study.observation.traits.changes.warning}]]*/ '',
	$.fieldbookMessages.errorServerError = /*[[#{common.error.server.error}]]*/ '';
$.fieldbookMessages.errorDesignGenerationFailed = /*[[#{common.design.generation.failed}]]*/ '';
$.fieldbookMessages.showFavoriteLocationHeader = /*[[#{show.favorite.location.header}]]*/ '';
$.fieldbookMessages.errorNoCurrentUser = /*[[#{error.no.current.user}]]*/ '';
$.fieldbookMessages.errorNoProgramMembers = /*[[#{error.no.program.members}]]*/ '';
$.fieldbookMessages.errorSubmittingListOwner = /*[[#{error.submit.list.owner}]]*/ '';
$.fieldbookMessages.errorNoDefaultValuesForCustomDesign = /*[[#{error.no.default.values.for.custom.design}]]*/ '';
$.fieldbookMessages.errorNoMappingSummary = /*[[#{error.no.mapping.summary}]]*/ '';
$.fieldbookMessages.errorSaveStudy = /*[[#{save.study.error}]]*/ '';
$.fieldbookMessages.errorNoVarietiesSamples = /*[[#{error.no.varieties.samples}]]*/ '';
$.fieldbookMessages.errorSaveSamplesList = /*[[#{error.save.samples}]]*/ '';


// Measurement observation messages
window.measurementObservationMessages = {
	showCategoricalDescription: /*[[#{observations.show.categorical.description}]]*/ '',
	hideCategoricalDescription: /*[[#{observations.hide.categorical.description}]]*/ ''
};

var cropName = 'maize';
var environmentConfirmLabel = '';

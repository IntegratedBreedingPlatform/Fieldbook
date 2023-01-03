package com.efficio.fieldbook.service;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.util.SettingsUtil;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 1/2/2015 Time: 9:48 AM
 */
public class SettingsServiceImpl implements SettingsService {

	private static final Logger LOG = LoggerFactory.getLogger(SettingsServiceImpl.class);

	/**
	 * The fieldbook service.
	 */
	@Resource
	protected FieldbookService fieldbookService;

	@Resource
	private ContextUtil contextUtil;

	@Resource
	private UserSelection studySelection;

	/**
	 * The fieldbook middleware service.
	 */
	@Resource
	protected org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

	@Override
	public SettingDetail createSettingDetail(final int id, final String name, final UserSelection userSelection,
			final int currentIbDbUserId, final String programUUID) {

		final String variableName;
		final StandardVariable stdVar = this.fieldbookMiddlewareService.getStandardVariable(id, programUUID);

		if (name != null && !name.isEmpty()) {
			variableName = name;
		} else {
			variableName = stdVar.getName();
		}

		if (stdVar != null && stdVar.getName() != null) {
			final SettingVariable svar =
					new SettingVariable(variableName, stdVar.getDescription(), stdVar.getProperty().getName(), stdVar.getScale().getName(),
							stdVar.getMethod().getName(), null, stdVar.getDataType().getName(), stdVar.getDataType().getId(),
							stdVar.getConstraints() != null && stdVar.getConstraints().getMinValue() != null ?
									stdVar.getConstraints().getMinValue() :
									null, stdVar.getConstraints() != null && stdVar.getConstraints().getMaxValue() != null ?
							stdVar.getConstraints().getMaxValue() :
							null);
			svar.setCvTermId(stdVar.getId());
			svar.setCropOntologyId(stdVar.getCropOntologyId() != null ? stdVar.getCropOntologyId() : "");
			svar.setTraitClass(stdVar.getIsA() != null ? stdVar.getIsA().getName() : "");
			svar.setOperation(Operation.ADD);

			final List<ValueReference> possibleValues = this.fieldbookService.getAllPossibleValues(id);
			final SettingDetail settingDetail = new SettingDetail(svar, possibleValues, null, false);
			settingDetail.setPossibleValuesToJson(possibleValues);
			return settingDetail;
		} else {
			final SettingVariable svar = new SettingVariable();
			svar.setCvTermId(stdVar.getId());
			return new SettingDetail(svar, null, null, false);
		}
	}

	public boolean isGermplasmListField(final Integer id) {

		try {
			final StandardVariable stdVar =
				this.fieldbookMiddlewareService.getStandardVariable(id, this.contextUtil.getCurrentProgramUUID());
			return SettingsUtil.hasVariableType(VariableType.GERMPLASM_DESCRIPTOR, stdVar.getVariableTypes());

		} catch (final MiddlewareException e) {
			SettingsServiceImpl.LOG.error(e.getMessage(), e);
		}

		return false;
	}

	/**
	 * Populates Setting Variable.
	 *
	 * @param var the var
	 */
	@Override
	public void populateSettingVariable(final SettingVariable var) {

		final StandardVariable stdvar = this.fieldbookMiddlewareService.getStandardVariable(var.getCvTermId(), this.contextUtil.getCurrentProgramUUID());
		if (stdvar != null) {
			var.setDescription(stdvar.getDescription());
			var.setProperty(stdvar.getProperty().getName());
			var.setScale(stdvar.getScale().getName());
			var.setMethod(stdvar.getMethod().getName());
			var.setDataType(stdvar.getDataType().getName());
			var.setVariableTypes(stdvar.getVariableTypes());
			var.setCropOntologyId(stdvar.getCropOntologyId() != null ? stdvar.getCropOntologyId() : "");
			var.setTraitClass(stdvar.getIsA() != null ? stdvar.getIsA().getName() : "");
			var.setDataTypeId(stdvar.getDataType().getId());
			var.setMinRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMinValue() != null
					? stdvar.getConstraints().getMinValue() : null);
			var.setMaxRange(stdvar.getConstraints() != null && stdvar.getConstraints().getMaxValue() != null
					? stdvar.getConstraints().getMaxValue() : null);
			var.setWidgetType();
		}
	}

	/**
	 * Adds the new setting details.
	 *
	 * @param mode       the mode
	 * @param newDetails the new details
	 * @throws Exception the exception
	 */
	@Override
	public void addNewSettingDetails(final int mode, final List<SettingDetail> newDetails) throws Exception {
		SettingsUtil.addNewSettingDetails(mode, newDetails, this.studySelection);
	}
}

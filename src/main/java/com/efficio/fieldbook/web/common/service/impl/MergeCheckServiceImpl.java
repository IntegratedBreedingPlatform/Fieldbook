
package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.nursery.form.ImportGermplasmListForm;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
@Transactional
public class MergeCheckServiceImpl implements MergeCheckService {

	@Override
	public List<ImportedGermplasm> mergeGermplasmList(List<ImportedGermplasm> primaryList, List<ImportedGermplasm> checkList,
			int startEntry, int interval, int manner, String defaultTestCheckId) {

		if (!this.isThereSomethingToMerge(primaryList, checkList, startEntry, interval)) {
			return primaryList;
		}

		List<ImportedGermplasm> newList = new ArrayList<ImportedGermplasm>();

		int primaryEntry = 1;
		int newEntry = 1;
		boolean isStarted = false;
		boolean shouldInsert = false;
		int checkIndex = 0;
		int intervalEntry = 0;
		for (ImportedGermplasm primaryGermplasm : primaryList) {
			if (primaryEntry == startEntry || intervalEntry == interval) {
				isStarted = true;
				shouldInsert = true;
				intervalEntry = 0;
			}

			if (isStarted) {
				intervalEntry++;
			}

			if (shouldInsert) {
				shouldInsert = false;
				List<ImportedGermplasm> checks = this.generateChecksToInsert(checkList, checkIndex, manner, newEntry);
				checkIndex++;
				newEntry += checks.size();
				intervalEntry += checks.size();

				newList.addAll(checks);
			}
			ImportedGermplasm primaryNewGermplasm = primaryGermplasm.copy();
			if (defaultTestCheckId != null && !"".equals(defaultTestCheckId)) {
				primaryNewGermplasm.setCheck(defaultTestCheckId);
				primaryNewGermplasm.setCheckId(Integer.valueOf(defaultTestCheckId));
			}
			newList.add(primaryNewGermplasm);
			newEntry++;

			primaryEntry++;
		}

		return newList;
	}

	private boolean isThereSomethingToMerge(List<ImportedGermplasm> primaryList, List<ImportedGermplasm> checkList, int startEntry,
			int interval) {
		boolean isThereSomethingToMerge = true;
		if (checkList == null || checkList.isEmpty()) {
			isThereSomethingToMerge = false;
		} else if (primaryList == null || primaryList.isEmpty()) {
			isThereSomethingToMerge = false;
		} else if (startEntry < 1 || startEntry > primaryList.size() || interval < 1) {
			isThereSomethingToMerge = false;
		}
		return isThereSomethingToMerge;
	}

	private List<ImportedGermplasm> generateChecksToInsert(List<ImportedGermplasm> checkList, int checkIndex, int manner, int newEntry) {
		List<ImportedGermplasm> newList = new ArrayList<ImportedGermplasm>();
		if (manner == AppConstants.MANNER_PER_LOCATION.getInt()) {
			for (ImportedGermplasm checkGerm : checkList) {
				newList.add(checkGerm.copy());
			}
		} else {
			int checkListIndex = checkIndex % checkList.size();
			ImportedGermplasm checkGerm = checkList.get(checkListIndex);
			newList.add(checkGerm.copy());
		}
		return newList;
	}

	@Override
	public void updatePrimaryListAndChecksBeforeMerge(ImportGermplasmListForm form) {
		String lastDragCheckList = form.getLastDraggedChecksList();
		if ("0".equalsIgnoreCase(lastDragCheckList)) {
			// this means the checks came from the same list
			List<ImportedGermplasm> newNurseryGermplasm =
					this.cleanGermplasmList(form.getImportedGermplasm(), form.getImportedCheckGermplasm());
			form.setImportedGermplasm(newNurseryGermplasm);
		} else {
			Integer entryNumber = form.getImportedGermplasm().size();
			for (ImportedGermplasm checkGerm : form.getImportedCheckGermplasm()) {
				entryNumber++;
				checkGerm.setEntryId(entryNumber);
			}
		}
	}

	protected List<ImportedGermplasm> cleanGermplasmList(List<ImportedGermplasm> primaryList, List<ImportedGermplasm> checkList) {
		if (checkList == null || checkList.isEmpty()) {
			return primaryList;
		}

		List<ImportedGermplasm> newPrimaryList = new ArrayList<>();
		Map<String, ImportedGermplasm> checkGermplasmMap = new HashMap<>();
		for (ImportedGermplasm checkGermplasm : checkList) {
			checkGermplasmMap.put(checkGermplasm.getGid() + "-" + checkGermplasm.getEntryId(), checkGermplasm);
		}

		for (ImportedGermplasm primaryGermplasm : primaryList) {
			if (checkGermplasmMap.get(primaryGermplasm.getGid() + "-" + primaryGermplasm.getEntryId()) == null) {
				newPrimaryList.add(primaryGermplasm);
			}
		}
		return newPrimaryList;
	}
}

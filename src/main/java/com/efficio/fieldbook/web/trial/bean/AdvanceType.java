package com.efficio.fieldbook.web.trial.bean;

@Deprecated
public enum AdvanceType {
	STUDY("study"), //
	SAMPLE("deprecatedSample"), //
	NONE("") //
	;

	private final String lowerCaseName;

	AdvanceType(final String lowerCaseName) {
		this.lowerCaseName = lowerCaseName;
	}

	public String getLowerCaseName() {
		return lowerCaseName;
	}

	public static AdvanceType fromLowerCaseName(final String lowerCaseName) {
		for (final AdvanceType advanceType : AdvanceType.values()) {
			if (advanceType.getLowerCaseName().equals(lowerCaseName)) {
				return advanceType;
			}
		}
		return AdvanceType.NONE;
	}
}

$(document).ready(function() {
	$('.help-tooltip').tooltip();
	$('.help-tooltip-nursery').tooltip();
	//this would handle the closing of modal since some modal window wont close on esc
	$('body').keydown(function(event) {
		if (event.keyCode === 27) {
			var length = $('.modal.in').length - 1;
			$($('.modal.in')[length]).modal('hide');
		}
	});
});
function createErrorNotification(titleDisplay, textDisplay) {
	'use strict';
	$('.error-notify').remove();
	createNotification('default-notification', titleDisplay, textDisplay, false, 'error-notify');
}
function createSuccessNotification(titleDisplay, textDisplay) {
	//we remove all error
	'use strict';
	$('.error-notify').remove();
	createNotification('default-notification', titleDisplay, textDisplay, 3000, '');
}
function createWarningNotification(titleDisplay, textDisplay, duration) {
	'use strict';
	if (! duration) {
		duration = 3000;
	}
	createNotification('default-notification', titleDisplay, textDisplay, duration, 'warning-notify');
}
function createNotification(template, titleDisplay, textDisplay, expires, className) {
	'use strict';
	var $container = $('#notification-container').notify();
	if (textDisplay !== null && textDisplay !== '' && textDisplay.length > 0) {
		if (textDisplay[textDisplay.length - 1] !== '.') {
			textDisplay += '.';
		}
	}
	var temp = $container.notify('create', template, {
		title: titleDisplay,
		text: textDisplay
	}, {
		expires: expires,
		click: function(e, instance) {
			instance.close();
		}
	});
	$(temp.element).addClass(className);
}

function isValidDate(dateString) {
	if (dateString === '') {
		return true;
	}
	var validPattern = /\d{4}-\d{1,2}-\d{1,2}/.test(dateString);
	if (!validPattern) {
		return false;
	}
	var parts = dateString.split('-');
	var year = parseInt(parts[0], 10);
	var month = parseInt(parts[1], 10);
	var day = parseInt(parts[2], 10);

	if (month === 0 || month > 12) {
		return false;
	}

	var monthLength = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

	if (year % 400 === 0 || (year % 100 !== 0 && year % 4 === 0)) {
		monthLength[1] = 29;
	}

	return day > 0 && day <= monthLength[month - 1];
}

function isValidYear(dateString) {
	if (dateString === '') {
		return true;
	}
	var parts = dateString.split('-');
	var year = parseInt(parts[0], 10);
	if (year >= 1900 && year <= 9999) {
		return true;
	}
	return false;
}

function padLeftByNumOfChars(value, toPad, numOfChars) {
	var leftPadding = '';
	var lengthOfValue = value.toString().length;
	if (lengthOfValue < numOfChars) {
		for (var i = lengthOfValue; i < numOfChars; i++) {
			leftPadding += toPad;
		}
	}
	var sliceFromIndex = -1 * parseInt(numOfChars);
	return (leftPadding + value).slice(sliceFromIndex);
}

function transformDate(dateString) {
	var pattern = /\d{1,4}-\d{1,2}-\d{1,2}/.test(dateString);
	if (pattern) {
		var delim = '-';
		var parts = dateString.split(delim);
		var year = parseInt(parts[0], 10);
		var month = parseInt(parts[1], 10);
		var day = parseInt(parts[2], 10);
		return padLeftByNumOfChars(year, '0', 4) + delim + padLeftByNumOfChars(month, '0', 2) + delim + padLeftByNumOfChars(day, '0', 2);
	}
	return dateString;
}

function validateAllDates() {
	var errorMsg = validateListOfDates('input[jq-datepicker]');
	if (errorMsg === '') {
		errorMsg = validateListOfDates('input.date-input');
	}
	return errorMsg;
}

function validateListOfDates(dateSelectorString) {
	var dateFormatErrorMsg = commonErrorDateFormat;
	var invalidYearErrorMsg = commonErrorInvalidYear;
	var errorMsg = '';
	$.each($(dateSelectorString), function(index, item) {
		$(item).val(transformDate($(item).val()));
		if (!isValidDate($(item).val())) {
			errorMsg = dateFormatErrorMsg;
			return false;
		} else if (!isValidYear($(item).val())) {
			errorMsg = invalidYearErrorMsg;
			return false;
		}
	});
	return errorMsg;
}

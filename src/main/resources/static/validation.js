function makePrettyDate(_s_date)
{
	var sYear = _s_date.substring(0, 4);
	var sMonth = _s_date.substring(5, 7);
	var sDay = _s_date.substring(8, 10);
	var sTime = _s_date.substring(11, 19);
	return (sMonth + "/" + sDay + "/" + sYear + " " + sTime);
}
	
function trimWhiteSpace(_s_clean_string) 
{
	var objRegExp = /^(\s*)([\W\w]*)(\b\s*$)/;
	if (objRegExp.test(_s_clean_string)) {
		_s_clean_string = _s_clean_string.replace(objRegExp, '$2');
	}
	
	objRegExp = /\r/g;
	if (objRegExp.test(_s_clean_string)) {
		_s_clean_string = _s_clean_string.replace(objRegExp, '');
	}
	
	objRegExp = /\n/g;
	if (objRegExp.test(_s_clean_string)) {
		_s_clean_string = _s_clean_string.replace(objRegExp, ' ');
	}
	
	objRegExp = /\s{2,}/g;
	if (objRegExp.test(_s_clean_string)) {
		_s_clean_string = _s_clean_string.replace(objRegExp, ' ');
	}

	return _s_clean_string;
}
	
	

function checkFieldRequired(_s_field_to_check, _s_validation_text)
{
	
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	
	/*
	
	if (_o_field_to_check == null)
	{
		alert("checkFieldRequired: " + _s_field_to_check + "," +  _o_field_to_check);
	}
	
	*/
	
	_o_field_to_check.value = trimWhiteSpace(_o_field_to_check.value);
	
	if (_o_field_to_check.value.length == 0)
	{
		alert ("'" + _s_validation_text + "' is required.")
		_o_field_to_check.focus();
		return false;
	}
	return true;
}


function checkFieldRequiredInput(_s_field_to_check, _s_validation_text)
{
	
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	
	_o_field_to_check.value = trimWhiteSpace(_o_field_to_check.value);
	
	if (_o_field_to_check.value == 'REQUIRED')
	{
		alert ("'" + _s_validation_text + "' is required.")
		_o_field_to_check.focus();
		return false;
	}
	return true;
}
	
	
	
function checkFieldRequiredCheck(_s_field_to_check, _s_validation_text)
{
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	if (_o_field_to_check == null)
	{
		alert("checkFieldRequired: " + _s_field_to_check + "," +  _o_field_to_check);
		return false;
	}
	if (_o_field_to_check.checked == false)
	{
		alert ("'" + _s_validation_text + "' is required to be checked.")
		_o_field_to_check.focus();
		return false;
	}
	return true;
}
	
	

function checkGoodSQLCharactersConvert(_s_field_to_check, _s_validation_text)
{
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	var sPattern = /[\"\'_]/g;
	var bMatch = sPattern.test(sStringToTest);

	if (bMatch)
	{
		var sNewString = sStringToTest.replace(sPattern, '`');
		_o_field_to_check.value = sNewString;
	}

}
	
function checkGoodSQLCharacters(_s_field_to_check, _s_validation_text)
{
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;

	if (_o_field_to_check.value != "")
	{
		var sPattern = /[\"\'_]/g;
		var bMatch = sPattern.test(sStringToTest);

		if (bMatch)
		{
			alert("Please do not enter any single-quotes, double-quotes or underscores in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
		return true;
	}
	else
	{
		return true;
	}			
}

function checkDigitsOnly(_s_field_to_check, _s_validation_text)
{
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;

	if (_o_field_to_check.value != "")
	{
		var sPattern = /[\D]/g;
		var bMatch = sPattern.test(sStringToTest);

		if (bMatch)
		{
			alert("Please enter only digits in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
		return true;
	}
	else
	{
		return true;
	}	

}
	
	
function checkNumericValue(_s_field_to_check, _s_validation_text, _n_minimum_value, _n_maximum_value)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		var sPattern = /^[-]?[0-9]*$/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
			alert("Please enter only digits in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
		
		if (_o_field_to_check.value >= _n_minimum_value && _o_field_to_check.value <= _n_maximum_value)
		{
			return true;
		}
		else
		{
			alert("Please enter a value between " + _n_minimum_value + " and " + _n_maximum_value + " in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
	}
	else
	{
		return true;
	}
}
	
function checkNumericValueDecimal(_s_field_to_check, _s_validation_text, _n_minimum_value, _n_maximum_value)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		var sPattern = /^[-]?([0-9])*\.([0-9]{2}$)/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
		
			var sMinimumLengthString = "" + _n_minimum_value;
			var sXPlaces = "";
			for (var i = 1; i < sMinimumLengthString.length; i++) {
				sXPlaces = sXPlaces + 'X';
			}
		
			alert("Please enter only digits in the '" + _s_validation_text + "' field in this format X" + sXPlaces  + ".XX");
			_o_field_to_check.focus();
			return false;
		}
		
		if (_o_field_to_check.value >= _n_minimum_value && _o_field_to_check.value <= _n_maximum_value)
		{
			return true;
		}
		else
		{
			alert("Please enter a value between " + _n_minimum_value + ".00 and " + _n_maximum_value + ".00 in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
	}
	else
	{
		return true;
	}
}
	
function checkFieldEMail(_s_field_to_check, _s_validation_text)
{
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	if (_o_field_to_check.value != "")
	{
		var sStringToTest = _o_field_to_check.value;
		var sPattern = /^[\w\.-]+@[\w\.-]+\.[\w\.-]+$/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
			alert("Please enter a valid e-mail in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
		return true;
	}
	else
	{
		return true;
	}
}

function checkFieldILLicensePlate(_s_field_to_check, _s_validation_text)
{
	var oFieldToCheck = document.getElementById(_s_field_to_check);
	if (oFieldToCheck.value != "")
	{
		var sStringToTest = oFieldToCheck.value;
		var sPattern = /^[0-9A-Za-z]{1,7}$/g;
		var bMatch = sPattern.test(sStringToTest);
		
		if (!bMatch)
		{
			alert("Please enter a valid Illinois License plate number in the '" + _s_validation_text + "' field. Do not include spaces, and only use numbers and letters.");
			oFieldToCheck.focus();
			return false;
		}
		return true;
	}
	else
	{
		return true;
	}
}
	
function checkFieldLength(_s_field_to_check, _s_validation_text, _n_minimum_length, _n_maximum_length, _b_trim_ws)
{
	
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	
	if (_o_field_to_check == null)
	{
		alert("checkFieldLength:" + _s_field_to_check + "," +  _o_field_to_check);
	}
	
	if (_b_trim_ws)
	{
		_o_field_to_check.value = trimWhiteSpace(_o_field_to_check.value);
	}
	
	if (_n_minimum_length == null || _n_minimum_length == 0)
	{
		if (_o_field_to_check.value.length > _n_maximum_length)
		{
			alert ("'" + _s_validation_text + "' cannot be greater than " + _n_maximum_length + " characters.")
			_o_field_to_check.focus();
			return false;
		}
	}
	else
	{
		s_error_text = "";
   
		if (_o_field_to_check.value.length < _n_minimum_length)
		{
			s_error_text = s_error_text + "'" + _s_validation_text + "' ";
			s_error_text = s_error_text + "cannot be less than " + _n_minimum_length + " characters";
		}
   
		if (_o_field_to_check.value.length > _n_maximum_length)
		{
			if (s_error_text != "")
			{
				s_error_text = s_error_text + "and\n   cannot be greater than " + _n_maximum_length + " characters";
			}
			else
			{
				s_error_text = s_error_text + "'" + _s_validation_text + "' cannot be greater than " + _n_maximum_length + " characters";
			}
		}
   
		if (s_error_text != "")
		{
			s_error_text = s_error_text + ".";
			alert (s_error_text);
			_o_field_to_check.focus();
			return false;
		}
	}
	return true;
}

var n_minimum_year = 1900;
var n_maximum_year = 2100;

function checkH2Date(_s_field_to_check, _s_validation_text)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		
		var sPattern = /^(\d{4})[-](\d{2})[-](\d{2})$/g;
		var bMatch = sPattern.test(sStringToTest);
		if (!bMatch) {
			alert("The date format of '" + _s_validation_text + "' should be : yyyy-mm-dd'");
			_o_field_to_check.focus();
			return false;
		}
		
		var n_days_in_month = new daysArray(12);
		
		var s_year_test = sStringToTest.substring(0, 4);
		var s_month_test = sStringToTest.substring(5, 7);
		var s_day_test = sStringToTest.substring(8, 10);
		
		var n_year = parseInt(s_year_test);
		var n_month = parseInt(s_month_test);
		var n_day = parseInt(s_day_test);
		
		if (n_month < 1 || n_month > 12)
		{
			alert("Please enter a valid month for '" + _s_validation_text + "'");
			_o_field_to_check.focus();
			return false;
		}
		
		if (n_day < 1 || n_day > 31 || (n_month == 2 && n_day > daysInFebruary(n_year)) || n_day > n_days_in_month[n_month])
		{
			alert("Please enter a valid day for '" + _s_validation_text + "'");
			_o_field_to_check.focus();
			return false;
		}
		
		if (n_year == 0 || n_year < n_minimum_year || n_year > n_maximum_year)
		{
			alert("Please enter a valid 4 digit year between " + n_minimum_year + " and " + n_maximum_year + " for '" + _s_validation_text + "'");
			_o_field_to_check.focus();
			return false;
		}
		
		return true;
		
	}
	else
	{
		return true;
	}
	
}

function checkDateTime(_s_field_to_check, _s_validation_text)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		var sPattern = /^(0[1-9]|1[012])[\/.](0[1-9]|[12][0-9]|3[01])[\/.]\d{4}\s[0-5][0-9]:[0-5][0-9]:[0-5][0-9]$/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
			alert("The date format of '" + _s_validation_text + "' should be : mm/dd/yyyy hh:mm:ss");
			_o_field_to_check.focus();
			return false;
		}
		
		var n_days_in_month = new daysArray(12);
		var n_pos_one = sStringToTest.indexOf('/');
		var n_pos_two = sStringToTest.indexOf('/', n_pos_one + 1);
		
		var s_month = sStringToTest.substring(0, n_pos_one);
		var s_day = sStringToTest.substring(n_pos_one + 1, n_pos_two);
		var s_year = sStringToTest.substring(n_pos_two + 1);

		if (s_month.substring(0, 1) == '0')
		{
			s_month = sStringToTest.substring(1, n_pos_one);
		}
		
		var n_month = parseInt(s_month);
		var n_day = parseInt(s_day);
		var n_year = parseInt(s_year);
		
		if (n_month < 1 || n_month > 12)
		{
			alert("Please enter a valid month for '" + _s_validation_text + "'");
			_o_field_to_check.focus();
			return false;
		}
		
		if (n_day < 1 || n_day > 31 || (n_month == 2 && n_day > daysInFebruary(n_year)) || n_day > n_days_in_month[n_month])
		{
			alert("Please enter a valid day for '" + _s_validation_text + "'");
			_o_field_to_check.focus();
			return false;
		}
		
		if (n_year == 0 || n_year < n_minimum_year || n_year > n_maximum_year)
		{
			alert("Please enter a valid 4 digit year between " + n_minimum_year + " and " + n_maximum_year + " for '" + _s_validation_text + "'");
			_o_field_to_check.focus();
			return false;
		}

		
		return true;
		
	}
	else
	{
		return true;
	}

}
	
	
function checkTime(_s_field_to_check, _s_validation_text)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		var sPattern = /^[0-5][0-9]:[0-5][0-9]:[0-5][0-9]$/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
			alert("The date format of '" + _s_validation_text + "' should be : hh:mm:ss");
			_o_field_to_check.focus();
			return false;
		}
		
		return true;
		
	}
	else
	{
		return true;
	}

}
	

function daysInFebruary (_n_year)
{
	return (((_n_year % 4 == 0) && ( (!(_n_year % 100 == 0)) || (_n_year % 400 == 0))) ? 29 : 28 );
}

class daysArray {
    constructor(n) {
        for (var i = 1; i <= n; i++) {
            this[i] = 31;
            if (i == 4 || i == 6 || i == 9 || i == 11) {
                this[i] = 30;
            }
            if (i == 2) {
                this[i] = 29;
            }
        }
        return this;
    }
}


function checkAlphaNumericOnlyCharacters(_s_field_to_check, _s_validation_text)
{
	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;

	if (_o_field_to_check.value != "")
	{
		var sPattern = /[\W]/g;
		var bMatch = sPattern.test(sStringToTest);

		if (bMatch)
		{
			alert("Please use only alpha and digit characters in '" + _s_validation_text + "' field.\nSpaces and special characters are not allowed either.");
			_o_field_to_check.focus();
			return false;
		}
		return true;
	}
	else
	{
		return true;
	}	

}
	
	
function makeFormDirty()
{
	var oDirtyInput = document.getElementById('IS_FORM_DIRTY');
	oDirtyInput.value = 'true';
}


function checkLengthOfFieldAndTabAutomatically(_oFieldToCheck, _nMaxLength, sFieldToTabTo)
{
	if (_oFieldToCheck.value.length > (_nMaxLength - 1))
	{

		var oFieldToTabTo = document.getElementById(sFieldToTabTo);
		
		var sFieldValue = "";		
		if (oFieldToTabTo != null)
		{
			sFieldValue = oFieldToTabTo.value;
			oFieldToTabTo.focus();
			oFieldToTabTo.value = sFieldValue;
		}		

	}
}
	
function setInputFormValue(_s_fieldname, _s_value)
{
	var oTargetCostCodeValueFieldName = document.getElementById(_s_fieldname);
	oTargetCostCodeValueFieldName.value = _s_value;
}




function checkNumericValueDecimalThreePlaces(_s_field_to_check, _s_validation_text, _n_minimum_value, _n_maximum_value)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		var sPattern = /^[-]?([0-9])*\.([0-9]{3}$)/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
		
			var sMinimumLengthString = "" + _n_minimum_value;
			var sXPlaces = "";
			for (var i = 1; i < sMinimumLengthString.length; i++) {
				sXPlaces = sXPlaces + 'X';
			}
		
			alert("Please enter only digits in the '" + _s_validation_text + "' field in this format X" + sXPlaces  + ".XXXXX");
			_o_field_to_check.focus();
			return false;
		}
		
		if (_o_field_to_check.value >= _n_minimum_value && _o_field_to_check.value <= _n_maximum_value)
		{
			return true;
		}
		else
		{
			alert("Please enter a value between " + _n_minimum_value + " and " + _n_maximum_value + " in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
	}
	else
	{
		return true;
	}
}

	

function checkNumericValueDecimalFivePlaces(_s_field_to_check, _s_validation_text, _n_minimum_value, _n_maximum_value)
{

	var _o_field_to_check = document.getElementById(_s_field_to_check);
	var sStringToTest = _o_field_to_check.value;
	
	if (_o_field_to_check.value != "")
	{
		var sPattern = /^[-]?([0-9])*\.([0-9]{5}$)/g;
		var bMatch = sPattern.test(sStringToTest);

		if (!bMatch)
		{
		
			var sMinimumLengthString = "" + _n_minimum_value;
			var sXPlaces = "";
			for (var i = 1; i < sMinimumLengthString.length; i++) {
				sXPlaces = sXPlaces + 'X';
			}
		
			alert("Please enter only digits in the '" + _s_validation_text + "' field in this format X" + sXPlaces  + ".XXXXX");
			_o_field_to_check.focus();
			return false;
		}
		
		if (_o_field_to_check.value >= _n_minimum_value && _o_field_to_check.value <= _n_maximum_value)
		{
			return true;
		}
		else
		{
			alert("Please enter a value between " + _n_minimum_value + " and " + _n_maximum_value + " in the '" + _s_validation_text + "' field.");
			_o_field_to_check.focus();
			return false;
		}
	}
	else
	{
		return true;
	}
}
Execute below request for create rules and then execute rules.

http://localhost:8080/createRules
{
	"obligationId": "123",
	"selectionCriteria": [
		{
			"groupName": "group 1",
			"condition": "and",
			"rules": [
				{
					"condition": "or",
					"key": "LTV",
					"operator": "==",
					"value": "85"
				},
				{
					"condition": "or",
					"key": "MortSpecial",
					"operator": "==",
					"value": "60"
				}
			]
		},
		{
			"groupName": "group 2",
			"condition": "and",
			"rules": [
				{
					"condition": "or",
					"key": "offer",
					"operator": "==",
					"value": "30"
				}
			]
		}
	]
}


http://localhost:8080/executeRules
{
"obligationId": "123",
  "loanToValue":"85",
  "mortSpecial":"60"
}

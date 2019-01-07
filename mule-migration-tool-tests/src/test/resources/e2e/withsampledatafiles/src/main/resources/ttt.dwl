%dw 1.0
%output application/json
---
{
	glossary: {
		title: payload.shiporder.orderperson
	}
}
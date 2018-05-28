%dw 1.0
%output application/java
---
{
    orderStatus: "complete" when flowVars.purchaseOrderStatus == "C" otherwise "incomplete"
}

%dw 2.0
output application/java  
---
{
  orderStatus: 
    if (vars.purchaseOrderStatus == "C")
      "complete"
    else
      "incomplete"
}
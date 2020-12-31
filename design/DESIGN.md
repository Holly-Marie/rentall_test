## subscribing
When users subscribe, a subscription is created with the chosen category. It also creates the first SubscriptionPeriod (which tracks the remaining downloads each month) and the first invoice is created with a price of 0.

## sharing a subscription
Sharing means adding a user to a subscription. The remaining content for all sharing subscribers comes for the current subscription period and so will reflect the same value for all users. The downloads or views will be subtracted for all shared users from the same subscription period.
An improvement would be to not just count down the remaining content, but track each access for statistics and maybe later machine learning.

## re-billing subscription
Re-billing would take the subscription, divide the price from the attached category. Then create an invoice for each sharing user with their fraction of the total price, attached to a new subscription period with the availableContent copied over from the category as the new remainingContent.

## scheduling re-billing
A scheduled task will run every night to check for subscription periods that expire soon (maybe less than 3 days left) and create the follow up period. That way there is still time to see if payment comes through and refuse service for the new period if payment did not come through.
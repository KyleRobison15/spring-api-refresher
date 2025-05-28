# Overview

## Stripe


### Checkout Session
* Tells Stripe what the User is paying for
* Gets a checkout URL that we can send to the client (basically outsources the checkout process)

#### Error Handling
There are several exceptions that can occur when working with Stripe for the checkout process:
* Invalid API Key
* Network Issues
* Bad Requests (e.g. negative amount for an item)
* Stripe service outage

We handle these errors by wrapping the business logic for creating a Stripe Session in a try-catch block. 
If there are errors, we delete the order to eliminate bad data in our DB, and pass the exception on to the Controller layer to be handled.

#### Webhook
When the client submits a request to check out and our server provides Stripe with the correct parameters for the Checkout Session, Stripe handles the actual payment.
In order to verify the result, we use a webhook

#### Working Stripe CLI
[Stripe CLI](https://docs.stripe.com/stripe-cli)

1. Install Stripe CLI (if not already installed)
2. Login to Stripe from the command line using "stripe login"
   1. HINT: at this point, your terminal window is logged into the Stripe server, so it can be useful to rename the terminal session as "Stripe Server"
3. Connect your local server with Stripe so Stripe can send us events
   1. "stripe listen --forward-to http://localhost:8080/checkout/webhook"
   2. Once connected, you will receive a Webhook signing secret for verifying Stripe's requests to your server
   3. Add the webhook secret key to your project env vars
4. Test the connection
   1. Create a new terminal session
   2. Trigger an event using "stripe trigger <event type>" (e.g. stripe trigger payment_intent.succeeded)
   3. Check the dashboard to view the event details [Stripe Dashboard](https://dashboard.stripe.com/test/dashboard)
   4. Review Code With Mosh course video for more details on understanding events, the lifecycle of a payment (payment_intent) and how transactions work in the Stripe ecosystem

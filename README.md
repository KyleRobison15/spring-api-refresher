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

We handle these errors by wrapping the business logic for creating a Stripe Session in a try-catch block. If there are errors, we delete the order to eliminate bad data in our DB, and pass the exception on to the Controller layer to be handled.

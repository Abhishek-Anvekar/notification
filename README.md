# How did you integrate the Twilio third-party API?

In my project, I integrated the Twilio API inside a notification service using Twilio’s Java SDK. First, I logged in to the Twilio website and collected AccountSID, AuthToken, and Twilio phoneNumber.
Then I keep all credentials in environment variables, and initialize Twilio through a configuration class, and send messages through a service layer.

Here, my notification-service receives events from other microservices like appointment-service through Kafka and we format a message to send via Twilio, like a confirmation message or a reminder message. 
when we send a message, Twilio returns messageSid and initial status that we store in the database to track delivery status.

While sending the SMS, I attach a statusCallback URL so that Twilio post the delivery updates to this endpoint automatically whenever the message status changes like queued, sent, delivered, or failed.
In controller method, I verified the Twilio signature of incoming request to ensure that the webhook request is coming from Twilio and not from an unauthorized source. Then updated the message status in my database.

In local development, I expose a temporary public callback URL using cloud dev environment (GitHub Codespaces, AWS or ngrok) so Twilio can reach my service for testing.
(The specific tool depends on the company’s policy — sometimes a secure tunneling tool, container ingress, or cloud dev port-forwarding is allowed.)

- **NOTE** - In demo project we used ngrok but in most companies, ngrok is restricted or blocked unless you get approval. Because ngrok exposes your local machine to the internet.

## Some companies use:

- cloud dev environment url

- ngrok paid version

- Cloudflare tunnel

- LocalTunnel

- Internal reverse proxy tools 


# YOUTUBE LINK FOR NGROK -

- [YouTube video for reference](https://youtu.be/EIkn-hEbhPM?si=k9PJmrzErx3LPzJ) 
- [YouTube video for reference](https://youtu.be/aFwrNSfthxU?si=rXRIy3E-P0VFtMO2) 
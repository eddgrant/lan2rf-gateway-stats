# Support LAN2RF devices with Basic Auth

Although disabled by default, LAN2RF devices can be configured to require basic auth in order to access them.
We should support this to ensure that security minded LAN2RF owners are able to use lan2rf-gateway-stats.

Somewhat terrifyingly, the LAN2RF doesn't support TLS, so if basic auth is enabled then the credentials have to be supplied over the network in plain text. There isn't much we can do about this, other than warning the user so that they are aware of the risks and use rotatable credentials that are only used to interact with the LAN2RF.

## Acceptance Criteria
1. It is possible, but optional, to provide basic auth credentials for LAN2RF devices.
2. The application should be able to connect to LAN2RF devices with basic auth enabled.
3. The application should be able to connect to LAN2RF devices with basic auth disabled. This is the default behaviour.
4. Where basic auth is configured, the application should handle basic auth errors gracefully and provide meaningful error messages.
5. The README file should contain a section which details the authentication behaviour. It will:
   * Explain the default behaviour (basic auth disabled).
   * Explain how to use basic auth.
   * Explain the risks of using basic auth with LAN2RF devices i.e. that they're using plain HTTP and the credentials are sent over the network unencrypted, so they should be rotated frequently and not reused for other purposes.
6. Existing relevant tests are updated to verify the basic auth behaviour.
7. Any new tests are written to verify the basic auth behaviour.
8. On startup, the application logs which authentication behaviour is configured (basic auth enabled or disabled).

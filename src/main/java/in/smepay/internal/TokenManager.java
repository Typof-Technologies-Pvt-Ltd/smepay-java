package in.smepay.internal;

import in.smepay.SMEPayException;
import java.util.Map;

/**
 * Logs in with client_id/client_secret and caches the access token.
 *
 * <p>Thread-safe: the lock is held during login, so concurrent threads share a single login.
 *
 * <p>Internal: not part of the public API; may change without notice.
 */
public final class TokenManager {
  private static final String AUTH_PATH = "/api/external/auth";

  /** Fetch a new token when less than this much of its lifetime is left. */
  private static final long REFRESH_MARGIN_NANOS = 60_000_000_000L;

  private final Http http;
  private final String baseUrl;
  private final String clientId;
  private final String clientSecret;

  private String token;

  /** {@link System#nanoTime()} at which the cached token expires. */
  private long expiresAtNanos;

  /** Creates a token manager; nothing is sent until the first {@link #getToken()}. */
  public TokenManager(Http http, String baseUrl, String clientId, String clientSecret) {
    this.http = http;
    this.baseUrl = baseUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  /**
   * Return the cached token, or log in if there is none or it is about to expire.
   *
   * @throws SMEPayException if the login fails
   */
  public synchronized String getToken() {
    if (token != null && System.nanoTime() - (expiresAtNanos - REFRESH_MARGIN_NANOS) < 0) {
      return token;
    }

    // Measured before the call so network time counts against the token's lifetime.
    long startedAt = System.nanoTime();
    Map<String, Object> reply =
        http.post(
            baseUrl, AUTH_PATH, Map.of("client_id", clientId, "client_secret", clientSecret), null);
    Object newToken = reply.get("access_token");
    Object expiresIn = reply.get("expires_in");
    if (!(newToken instanceof String)
        || ((String) newToken).isEmpty()
        || !(expiresIn instanceof Number)
        || ((Number) expiresIn).doubleValue() <= 0) {
      throw new SMEPayException(
          "invalid_response",
          "SMEPay auth reply did not contain an access token",
          200,
          reply,
          null);
    }

    token = (String) newToken;
    expiresAtNanos = startedAt + (long) (((Number) expiresIn).doubleValue() * 1_000_000_000L);
    return token;
  }

  /** Drop the cached token if it is the one the server just rejected. */
  public synchronized void invalidate(String rejected) {
    if (rejected.equals(token)) {
      token = null;
      expiresAtNanos = 0;
    }
  }

  @Override
  public String toString() {
    return "TokenManager{}";
  }
}

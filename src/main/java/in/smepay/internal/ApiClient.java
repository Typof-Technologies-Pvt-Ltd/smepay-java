package in.smepay.internal;

import in.smepay.SMEPayException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Authenticated POSTs with client_id added to every body.
 *
 * <p>Internal: not part of the public API; may change without notice.
 */
public final class ApiClient {
  /** 401 codes meaning the token itself was rejected; a fresh login may fix these. */
  private static final Set<String> TOKEN_ERROR_CODES =
      Set.of("invalid_token", "invalid_external_token", "invalid_token_type");

  private final Http http;
  private final String baseUrl;
  private final String clientId;
  private final TokenManager auth;

  /** Creates a client for one base URL and set of credentials. */
  public ApiClient(String baseUrl, String clientId, String clientSecret) {
    this(new Http(), baseUrl, clientId, clientSecret);
  }

  ApiClient(Http http, String baseUrl, String clientId, String clientSecret) {
    this.http = http;
    this.baseUrl = baseUrl;
    this.clientId = clientId;
    this.auth = new TokenManager(http, baseUrl, clientId, clientSecret);
  }

  /**
   * POST {@code params} plus {@code client_id} to {@code path}; on a rejected token, log in again
   * once and repeat the call once.
   *
   * @throws SMEPayException on any failure
   */
  public Map<String, Object> post(String path, Map<String, ?> params) {
    Map<String, Object> body = new LinkedHashMap<>(params);
    body.put("client_id", clientId);
    String token = auth.getToken();
    try {
      return http.post(baseUrl, path, body, token);
    } catch (SMEPayException e) {
      Integer status = e.getStatusCode();
      if (status == null || status != 401 || !TOKEN_ERROR_CODES.contains(e.getCode())) {
        throw e;
      }
    }

    // Token rejected (e.g. server session lost): log in again once and repeat the call once.
    auth.invalidate(token);
    return http.post(baseUrl, path, body, auth.getToken());
  }

  @Override
  public String toString() {
    return "ApiClient{}";
  }
}

package in.smepay;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Thrown for every failed SDK call.
 *
 * <p>API errors: {@link #getCode()} is the API's {@code error} field (e.g. {@code
 * "duplicate_order_id"}), {@link #getStatusCode()} the HTTP status and {@link #getRaw()} the API
 * reply. SDK-side errors use the codes {@code "invalid_config"}, {@code "invalid_request"}, {@code
 * "network_error"}, {@code "timeout"} and {@code "invalid_response"}; {@link #getStatusCode()} is
 * {@code null} when no reply was received.
 */
public class SMEPayException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** Keys that must never appear in an error, even if an API reply echoes them. */
  private static final Set<String> SENSITIVE_KEYS =
      Set.of("access_token", "client_secret", "authorization");

  /** Machine-readable error code. */
  private final String code;

  /** HTTP status of the reply, or {@code null} if none was received. */
  private final Integer statusCode;

  /** The API reply with secrets redacted; not serialized. */
  private final transient Object raw;

  /**
   * Creates an exception.
   *
   * @param code machine-readable error code, e.g. {@code "duplicate_order_id"}
   * @param message human-readable message
   * @param statusCode HTTP status of the reply, or {@code null} if none was received
   * @param raw the API reply (decoded JSON or a text excerpt), or {@code null}
   * @param cause the underlying exception, or {@code null}
   */
  public SMEPayException(
      String code, String message, Integer statusCode, Object raw, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.statusCode = statusCode;
    this.raw = redact(raw);
  }

  /**
   * Creates an exception with no reply and no cause.
   *
   * @param code machine-readable error code
   * @param message human-readable message
   */
  public SMEPayException(String code, String message) {
    this(code, message, null, null, null);
  }

  /**
   * Machine-readable code, e.g. {@code "duplicate_order_id"} or {@code "timeout"}.
   *
   * @return the error code
   */
  public String getCode() {
    return code;
  }

  /**
   * HTTP status of the API reply.
   *
   * @return the status, or {@code null} if no reply was received
   */
  public Integer getStatusCode() {
    return statusCode;
  }

  /**
   * The API reply with secrets redacted: a {@code Map<String, Object>} for JSON objects, a {@code
   * List} or value for other JSON, a {@code String} excerpt for non-JSON replies.
   *
   * @return the reply, or {@code null}
   */
  public Object getRaw() {
    return raw;
  }

  private static Object redact(Object value) {
    if (!(value instanceof Map)) {
      return value;
    }
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
      String key = String.valueOf(entry.getKey());
      boolean sensitive = SENSITIVE_KEYS.contains(key.toLowerCase(Locale.ROOT));
      copy.put(key, sensitive ? "[redacted]" : entry.getValue());
    }
    return Collections.unmodifiableMap(copy);
  }
}

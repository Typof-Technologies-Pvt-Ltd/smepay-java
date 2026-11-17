package in.smepay.internal;

import in.smepay.SMEPayException;
import in.smepay.Version;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * JSON-over-HTTPS POST helper.
 *
 * <p>Internal: not part of the public API; may change without notice.
 */
public final class Http {
  /** Fixed maximum per request; deliberately not configurable. */
  public static final Duration TIMEOUT = Duration.ofSeconds(60);

  /** Longest slice of a non-JSON reply kept in {@link SMEPayException#getRaw()}. */
  private static final int RAW_TEXT_LIMIT = 500;

  /** Cap on the size of a reply. */
  private static final int MAX_REPLY_BYTES = 10 * 1024 * 1024;

  private final HttpClient client;
  private final Duration timeout;

  /** Creates a helper with the fixed 60s timeout. */
  public Http() {
    this(TIMEOUT);
  }

  Http(Duration timeout) {
    this.timeout = timeout;
    this.client =
        HttpClient.newBuilder()
            .connectTimeout(timeout)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
  }

  /**
   * POST {@code body} as JSON and return the decoded reply object.
   *
   * @param token Bearer token, or {@code null} for none
   * @throws SMEPayException on any failure
   */
  public Map<String, Object> post(String baseUrl, String path, Map<String, ?> body, String token) {
    String payload;
    try {
      payload = Json.write(body);
    } catch (Json.JsonException e) {
      throw new SMEPayException(
          "invalid_request",
          "Request body for " + path + " could not be encoded as JSON: " + e.getMessage());
    }

    HttpRequest.Builder request =
        HttpRequest.newBuilder(URI.create(baseUrl + path))
            .timeout(timeout)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "smepay-java/" + Version.VERSION)
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
    if (token != null && !token.isEmpty()) {
      request.header("Authorization", "Bearer " + token);
    }

    int status;
    String text;
    try {
      HttpResponse<InputStream> response =
          client.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
      status = response.statusCode();
      try (InputStream in = response.body()) {
        byte[] bytes = in.readNBytes(MAX_REPLY_BYTES + 1);
        if (bytes.length > MAX_REPLY_BYTES) {
          throw new SMEPayException(
              "invalid_response", "SMEPay reply for " + path + " is too large", status, null, null);
        }
        text = new String(bytes, StandardCharsets.UTF_8);
      }
    } catch (HttpTimeoutException e) {
      throw new SMEPayException(
          "timeout",
          "Request to " + path + " timed out after " + timeout.toSeconds() + "s",
          null,
          null,
          e);
    } catch (IOException e) {
      throw new SMEPayException(
          "network_error", "Could not reach SMEPay (" + path + "): " + describe(e), null, null, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SMEPayException(
          "network_error", "Request to " + path + " was interrupted", null, null, e);
    }

    Object data;
    try {
      data = Json.parse(text);
    } catch (Json.JsonException e) {
      throw new SMEPayException(
          "invalid_response",
          "SMEPay returned a non-JSON reply (HTTP " + status + ") for " + path,
          status,
          text.length() > RAW_TEXT_LIMIT ? text.substring(0, RAW_TEXT_LIMIT) : text,
          null);
    }

    if (status < 200 || status >= 300) {
      throw apiError(status, data);
    }
    if (!(data instanceof Map)) {
      throw new SMEPayException(
          "invalid_response",
          "SMEPay reply for " + path + " was not a JSON object",
          status,
          data,
          null);
    }
    @SuppressWarnings("unchecked") // Json.parse builds objects as Map<String, Object>
    Map<String, Object> map = (Map<String, Object>) data;
    return map;
  }

  /** Build an exception from an API error reply: {error, message} or {error, errors}. */
  private static SMEPayException apiError(int status, Object data) {
    Map<?, ?> body = data instanceof Map ? (Map<?, ?>) data : Map.of();
    Object error = body.get("error");
    String code =
        error instanceof String && !((String) error).isEmpty() ? (String) error : "api_error";

    String message = "SMEPay API error (HTTP " + status + ")";
    Object text = body.get("message");
    Object errors = body.get("errors");
    if (text instanceof String && !((String) text).isEmpty()) {
      message = (String) text;
    } else if (errors instanceof String && !((String) errors).isEmpty()) {
      message = (String) errors;
    } else if (errors != null) {
      try {
        message = Json.write(errors);
      } catch (Json.JsonException e) {
        message = String.valueOf(errors);
      }
    }
    return new SMEPayException(code, message, status, data, null);
  }

  private static String describe(IOException e) {
    String message = e.getMessage();
    return message == null || message.isEmpty() ? e.getClass().getSimpleName() : message;
  }
}

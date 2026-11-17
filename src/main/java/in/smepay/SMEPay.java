package in.smepay;

import in.smepay.internal.ApiClient;

/**
 * SMEPay External API client. Thread-safe; create one and reuse it.
 *
 * <pre>{@code
 * SMEPay client = SMEPay.builder()
 *     .clientId(System.getenv("SMEPAY_CLIENT_ID"))
 *     .clientSecret(System.getenv("SMEPAY_CLIENT_SECRET"))
 *     .environment(Environment.TEST) // TEST (default) or LIVE
 *     .build();
 * }</pre>
 *
 * <p>The environment must match your business's mode on SMEPay. Bad config throws {@link
 * SMEPayException} {@code invalid_config}.
 */
public final class SMEPay {
  private final Environment environment;
  final ApiClient api;

  private SMEPay(Environment environment, ApiClient api) {
    this.environment = environment;
    this.api = api;
  }

  /**
   * Starts building a client.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * The SMEPay environment this client uses.
   *
   * @return {@link Environment#TEST} or {@link Environment#LIVE}
   */
  public Environment getEnvironment() {
    return environment;
  }

  @Override
  public String toString() {
    return "SMEPay{environment=" + environment + "}";
  }

  /** Builds an {@link SMEPay} client. */
  public static final class Builder {
    private String clientId;
    private String clientSecret;
    private Environment environment = Environment.TEST;

    private Builder() {}

    /**
     * Your client ID from the SMEPay dashboard (required).
     *
     * @param clientId the client ID
     * @return this builder
     */
    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    /**
     * Your client secret from the SMEPay dashboard (required). Keep it in an environment variable.
     *
     * @param clientSecret the client secret
     * @return this builder
     */
    public Builder clientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
    }

    /**
     * {@link Environment#TEST} (default) or {@link Environment#LIVE}.
     *
     * @param environment the environment
     * @return this builder
     */
    public Builder environment(Environment environment) {
      this.environment = environment;
      return this;
    }

    /**
     * Creates the client. Nothing is sent until the first call.
     *
     * @return the client
     * @throws SMEPayException {@code invalid_config} if a value is missing or blank
     */
    public SMEPay build() {
      // Also catches null from an unset environment variable.
      if (clientId == null || clientId.isBlank()) {
        throw new SMEPayException("invalid_config", "clientId is required");
      }
      if (clientSecret == null || clientSecret.isBlank()) {
        throw new SMEPayException("invalid_config", "clientSecret is required");
      }
      if (environment == null) {
        throw new SMEPayException("invalid_config", "environment must be TEST or LIVE");
      }
      return new SMEPay(
          environment,
          new ApiClient(environment.baseUrl(), clientId.strip(), clientSecret.strip()));
    }

    @Override
    public String toString() {
      return "SMEPay.Builder{environment=" + environment + "}";
    }
  }
}

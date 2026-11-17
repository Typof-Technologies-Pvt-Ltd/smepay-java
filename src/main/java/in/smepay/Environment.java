package in.smepay;

/** The SMEPay environment a client talks to; it must match your business's mode on SMEPay. */
public enum Environment {
  /** Test mode: {@code https://staging.smepay.in}; no real money. */
  TEST("https://staging.smepay.in"),
  /** Live mode: {@code https://extranet.smepay.in}; real payments. */
  LIVE("https://extranet.smepay.in");

  private final String baseUrl;

  Environment(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  String baseUrl() {
    return baseUrl;
  }
}

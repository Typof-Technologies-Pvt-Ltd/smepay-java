package in.smepay;

import java.util.LinkedHashMap;
import java.util.Map;

/** Request for {@link Payment#create(CreateParams)}. */
public final class CreateParams {
  private final String amount;
  private final String orderId;
  private final String callbackUrl;
  private final CustomerDetails customerDetails;

  private CreateParams(Builder b) {
    this.amount = b.amount;
    this.orderId = b.orderId;
    this.callbackUrl = b.callbackUrl;
    this.customerDetails = b.customerDetails;
  }

  /**
   * Starts building a create request.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("amount", amount);
    map.put("order_id", orderId);
    map.put("callback_url", callbackUrl);
    map.put("customer_details", customerDetails.toMap());
    return map;
  }

  /** Builds {@link CreateParams}; all four values are required. */
  public static final class Builder {
    private String amount;
    private String orderId;
    private String callbackUrl;
    private CustomerDetails customerDetails;

    private Builder() {}

    /**
     * Amount in rupees, as a string, e.g. {@code "100.50"}.
     *
     * @param amount the amount
     * @return this builder
     */
    public Builder amount(String amount) {
      this.amount = amount;
      return this;
    }

    /**
     * Your own order reference; must be unique for your business.
     *
     * @param orderId the order reference
     * @return this builder
     */
    public Builder orderId(String orderId) {
      this.orderId = orderId;
      return this;
    }

    /**
     * URL that receives the payment result webhook.
     *
     * @param callbackUrl the callback URL
     * @return this builder
     */
    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    /**
     * Payer details; email or mobile required.
     *
     * @param customerDetails the payer details
     * @return this builder
     */
    public Builder customerDetails(CustomerDetails customerDetails) {
      this.customerDetails = customerDetails;
      return this;
    }

    /**
     * Creates the request.
     *
     * @return the request
     * @throws SMEPayException {@code invalid_request} if a value is missing
     */
    public CreateParams build() {
      Params.require(amount, "amount");
      Params.require(orderId, "orderId");
      Params.require(callbackUrl, "callbackUrl");
      if (customerDetails == null) {
        throw new SMEPayException("invalid_request", "customerDetails is required");
      }
      return new CreateParams(this);
    }
  }
}

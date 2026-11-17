package in.smepay;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request for {@link Payment#getStatus(GetStatusParams)}: SMEPay's {@code orderId} or your {@code
 * externalReferenceId} (the latter is used first if both are set).
 */
public final class GetStatusParams {
  private final String orderId;
  private final String externalReferenceId;
  private final String callbackUrl;

  private GetStatusParams(Builder b) {
    this.orderId = b.orderId;
    this.externalReferenceId = b.externalReferenceId;
    this.callbackUrl = b.callbackUrl;
  }

  /**
   * Starts building a status request.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    if (orderId != null) {
      map.put("order_id", orderId);
    }
    if (externalReferenceId != null) {
      map.put("external_reference_id", externalReferenceId);
    }
    if (callbackUrl != null) {
      map.put("callback_url", callbackUrl);
    }
    return map;
  }

  /** Builds {@link GetStatusParams}. */
  public static final class Builder {
    private String orderId;
    private String externalReferenceId;
    private String callbackUrl;

    private Builder() {}

    /**
     * SMEPay's order ID ({@link CreateResponse#getOrderId()}).
     *
     * @param orderId SMEPay's order ID
     * @return this builder
     */
    public Builder orderId(String orderId) {
      this.orderId = orderId;
      return this;
    }

    /**
     * Your own order reference (the {@code orderId} you passed to create).
     *
     * @param externalReferenceId your order reference
     * @return this builder
     */
    public Builder externalReferenceId(String externalReferenceId) {
      this.externalReferenceId = externalReferenceId;
      return this;
    }

    /**
     * Optional; saved only if the order has no callback URL yet.
     *
     * @param callbackUrl the callback URL
     * @return this builder
     */
    public Builder callbackUrl(String callbackUrl) {
      this.callbackUrl = callbackUrl;
      return this;
    }

    /**
     * Creates the request.
     *
     * @return the request
     * @throws SMEPayException {@code invalid_request} if neither ID is set
     */
    public GetStatusParams build() {
      if (Params.blank(orderId) && Params.blank(externalReferenceId)) {
        throw new SMEPayException("invalid_request", "orderId or externalReferenceId is required");
      }
      return new GetStatusParams(this);
    }
  }
}

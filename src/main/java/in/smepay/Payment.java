package in.smepay;

import in.smepay.internal.ApiClient;
import java.util.Map;

/** Payment calls: create → initiate → getStatus. Use it as {@code client.payment()}. */
public final class Payment {
  private final ApiClient api;

  Payment(ApiClient api) {
    this.api = api;
  }

  /**
   * Create a payment order. Next step: {@code initiate(order.getOrderSlug())}.
   *
   * @param params amount, your order ID, callback URL and payer details
   * @return the new order
   * @throws SMEPayException on any failure
   */
  public CreateResponse create(CreateParams params) {
    if (params == null) {
      throw new SMEPayException("invalid_request", "params is required");
    }
    return new CreateResponse(api.post("/api/external/order/create", params.toMap()));
  }

  /**
   * Start the payment for an order; returns the UPI link, QR code and app links.
   *
   * @param slug {@link CreateResponse#getOrderSlug()}
   * @return the payment details to show the customer
   * @throws SMEPayException on any failure
   */
  public InitiateResponse initiate(String slug) {
    Params.require(slug, "slug");
    return new InitiateResponse(api.post("/api/external/order/initiate", Map.of("slug", slug)));
  }

  /**
   * Fetch the current payment status of an order.
   *
   * @param params SMEPay's order ID or your order reference, optional callback URL
   * @return the status
   * @throws SMEPayException on any failure
   */
  public GetStatusResponse getStatus(GetStatusParams params) {
    if (params == null) {
      throw new SMEPayException("invalid_request", "params is required");
    }
    return new GetStatusResponse(api.post("/api/external/order/status", params.toMap()));
  }

  @Override
  public String toString() {
    return "Payment{}";
  }
}

package in.smepay;

import java.util.Map;

/** Reply of {@link Payment#initiate(String)}. */
public final class InitiateResponse {
  private final Map<String, Object> raw;

  InitiateResponse(Map<String, Object> raw) {
    this.raw = raw;
  }

  /**
   * {@code status}: whether the call succeeded.
   *
   * @return true on success
   */
  public boolean getStatus() {
    return Replies.bool(raw, "status");
  }

  /**
   * {@code order_id}: SMEPay's order ID.
   *
   * @return SMEPay's order ID
   */
  public String getOrderId() {
    return Replies.string(raw, "order_id");
  }

  /**
   * {@code external_reference_id}: your order reference.
   *
   * @return your order reference
   */
  public String getExternalReferenceId() {
    return Replies.string(raw, "external_reference_id");
  }

  /**
   * {@code payment_link}: UPI payment link ({@code upi://pay?...}).
   *
   * @return the UPI link
   */
  public String getPaymentLink() {
    return Replies.string(raw, "payment_link");
  }

  /**
   * {@code transaction_id}.
   *
   * @return the transaction ID
   */
  public String getTransactionId() {
    return Replies.string(raw, "transaction_id");
  }

  /**
   * {@code qr_code}: base64-encoded PNG to show the customer.
   *
   * @return the QR code, base64
   */
  public String getQrCode() {
    return Replies.string(raw, "qr_code");
  }

  /**
   * {@code payment_status}, e.g. {@link PaymentStatus#INITIATED}.
   *
   * @return the payment status
   */
  public String getPaymentStatus() {
    return Replies.string(raw, "payment_status");
  }

  /**
   * {@code expires_at}: when the payment window closes, Unix seconds.
   *
   * @return the expiry, or {@code null} if not in the reply
   */
  public Long getExpiresAt() {
    return Replies.epochSeconds(raw, "expires_at");
  }

  /**
   * {@code intents}: UPI app key (e.g. {@code "gpay"}) to that app's payment link.
   *
   * @return the app links (read-only)
   */
  public Map<String, String> getIntents() {
    return Replies.stringMap(raw, "intents");
  }

  /**
   * {@code message} from SMEPay.
   *
   * @return the message
   */
  public String getMessage() {
    return Replies.string(raw, "message");
  }

  /**
   * The full reply, keys exactly the API's (read-only).
   *
   * @return the reply map
   */
  public Map<String, Object> getRaw() {
    return raw;
  }

  @Override
  public String toString() {
    return "InitiateResponse" + raw;
  }
}

package in.smepay;

import java.math.BigDecimal;
import java.util.Map;

/** Reply of {@link Payment#getStatus(GetStatusParams)}. */
public final class GetStatusResponse {
  private final Map<String, Object> raw;

  GetStatusResponse(Map<String, Object> raw) {
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
   * {@code external_reference_id}: your order reference.
   *
   * @return your order reference
   */
  public String getExternalReferenceId() {
    return Replies.string(raw, "external_reference_id");
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
   * {@code payment_status}, e.g. {@link PaymentStatus#SUCCESS}.
   *
   * @return the payment status
   */
  public String getPaymentStatus() {
    return Replies.string(raw, "payment_status");
  }

  /**
   * {@code amount} in rupees.
   *
   * @return the amount
   */
  public BigDecimal getAmount() {
    return Replies.decimal(raw, "amount");
  }

  /**
   * {@code created_at}: Unix seconds.
   *
   * @return the creation time, or {@code null} if not in the reply
   */
  public Long getCreatedAt() {
    return Replies.epochSeconds(raw, "created_at");
  }

  /**
   * {@code processed_at}: Unix seconds.
   *
   * @return the processing time, or {@code null} until processed
   */
  public Long getProcessedAt() {
    return Replies.epochSeconds(raw, "processed_at");
  }

  /**
   * {@code utr}: bank reference number.
   *
   * @return the UTR, or {@code null} until available
   */
  public String getUtr() {
    return Replies.string(raw, "utr");
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
    return "GetStatusResponse" + raw;
  }
}

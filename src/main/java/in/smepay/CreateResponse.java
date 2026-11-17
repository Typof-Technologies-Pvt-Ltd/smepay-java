package in.smepay;

import java.math.BigDecimal;
import java.util.Map;

/** Reply of {@link Payment#create(CreateParams)}. */
public final class CreateResponse {
  private final Map<String, Object> raw;

  CreateResponse(Map<String, Object> raw) {
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
   * {@code order_slug}: pass to {@link Payment#initiate(String)}.
   *
   * @return the order slug
   */
  public String getOrderSlug() {
    return Replies.string(raw, "order_slug");
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
   * {@code amount} in rupees.
   *
   * @return the amount
   */
  public BigDecimal getAmount() {
    return Replies.decimal(raw, "amount");
  }

  /**
   * {@code payment_status}, e.g. {@link PaymentStatus#CREATED}.
   *
   * @return the payment status
   */
  public String getPaymentStatus() {
    return Replies.string(raw, "payment_status");
  }

  /**
   * {@code payment_url}: hosted SMEPay payment page.
   *
   * @return the payment page URL
   */
  public String getPaymentUrl() {
    return Replies.string(raw, "payment_url");
  }

  /**
   * {@code ref_id}: your order reference, echoed back.
   *
   * @return your order reference
   */
  public String getRefId() {
    return Replies.string(raw, "ref_id");
  }

  /**
   * {@code customer_details} as sent.
   *
   * @return the payer details
   */
  public Map<String, String> getCustomerDetails() {
    return Replies.stringMap(raw, "customer_details");
  }

  /**
   * {@code callback_url}.
   *
   * @return the callback URL, or {@code null} if not in the reply
   */
  public String getCallbackUrl() {
    return Replies.string(raw, "callback_url");
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
    return "CreateResponse" + raw;
  }
}

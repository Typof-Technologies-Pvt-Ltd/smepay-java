package in.smepay;

import java.util.LinkedHashMap;
import java.util.Map;

/** Payer details for {@link CreateParams}; at least an email or a mobile number is required. */
public final class CustomerDetails {
  private final String name;
  private final String email;
  private final String mobile;

  private CustomerDetails(String name, String email, String mobile) {
    this.name = name;
    this.email = email;
    this.mobile = mobile;
  }

  /**
   * Payer with only a mobile number.
   *
   * @param mobile mobile number, e.g. {@code "9876543210"}
   * @return the details
   */
  public static CustomerDetails ofMobile(String mobile) {
    return builder().mobile(mobile).build();
  }

  /**
   * Payer with only an email address.
   *
   * @param email email address
   * @return the details
   */
  public static CustomerDetails ofEmail(String email) {
    return builder().email(email).build();
  }

  /**
   * Starts building payer details.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    if (name != null) {
      map.put("name", name);
    }
    if (email != null) {
      map.put("email", email);
    }
    if (mobile != null) {
      map.put("mobile", mobile);
    }
    return map;
  }

  /** Builds {@link CustomerDetails}. */
  public static final class Builder {
    private String name;
    private String email;
    private String mobile;

    private Builder() {}

    /**
     * Payer name (optional).
     *
     * @param name the name
     * @return this builder
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * Payer email (email or mobile required).
     *
     * @param email the email
     * @return this builder
     */
    public Builder email(String email) {
      this.email = email;
      return this;
    }

    /**
     * Payer mobile number (email or mobile required).
     *
     * @param mobile the mobile number
     * @return this builder
     */
    public Builder mobile(String mobile) {
      this.mobile = mobile;
      return this;
    }

    /**
     * Creates the details.
     *
     * @return the details
     * @throws SMEPayException {@code invalid_request} if neither email nor mobile is set
     */
    public CustomerDetails build() {
      if (Params.blank(email) && Params.blank(mobile)) {
        throw new SMEPayException("invalid_request", "customerDetails needs an email or mobile");
      }
      return new CustomerDetails(name, email, mobile);
    }
  }
}

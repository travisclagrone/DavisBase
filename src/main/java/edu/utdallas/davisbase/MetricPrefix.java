package edu.utdallas.davisbase;

import java.math.BigDecimal;

/**
 * An enumerated type of the metric unit prefixes defined by the International System of Units (SI).
 * Only decadic (base-10) prefixes officially recognized by SI are included here.
 */
public enum MetricPrefix {
  YOTTA ("yotta", "Y",  24, "septillion"),
  ZETTA ("zetta", "Z",  21, "sextillion"),
  EXA   ("exa",   "E",  18, "quintillion"),
  PETA  ("peta",  "P",  15, "quadrillion"),
  TERA  ("tera",  "T",  12, "trillion"),
  GIGA  ("giga",  "G",   9, "billion"),
  MEGA  ("mega",  "M",   6, "million"),
  KILO  ("kilo",  "k",   3, "thousand"),
  HECTO ("hecto", "h",   2, "hundred"),
  DECA  ("deca",  "da",  1, "ten"),
  DECI  ("deci",  "d",  -1, "tenth"),
  CENTI ("centi", "c",  -2, "hundredth"),
  MILLI ("milli", "m",  -3, "thousandth"),
  MICRO ("micro", "μ",  -6, "millionth"),
  NANO  ("nano",  "n",  -9, "billionth"),
  PICO  ("pico",  "p", -12, "trillionth"),
  FEMTO ("femto", "f", -15, "quadrillionth"),
  ATTO  ("atto",  "a", -18, "quintillionth"),
  ZEPTO ("zepto", "z", -21, "sextillionth"),
  YOCTO ("yocto", "y", -24, "septillionth");

  private final String prefix;
  private final String symbol;
  private final int exponent10;
  private final BigDecimal decimalValue;
  private final String word;

  private MetricPrefix(String prefix, String symbol, int exponent10, String word) {
    this.prefix = prefix;
    this.symbol = symbol;
    this.exponent10 = exponent10;
    this.decimalValue = BigDecimal.TEN.pow(exponent10);
    this.word = word;
  }

  /**
   * @return the SI prefix for this metric prefix (lowercase, no trailing hyphen)
   */
  public String prefix() {
    return prefix;
  }

  /**
   * @return the SI symbol for this metric prefix (case-sensitive, not all ASCII, and most
   *         single-character but some multi-character)
   */
  public String symbol() {
    return symbol;
  }

  /**
   * @return the base-10 exponent of the integral power of 10 corresponding to this metric prefix
   */
  public int exponent10() {
    return exponent10;
  }

  /**
   * @return the exact numeric value corresponding to this metric prefix as a {@link BigDecimal}
   */
  public BigDecimal decimalValue() {
    return decimalValue;
  }

  /**
   * @return the English word for the integral power of 10 corresponding to this metric prefix,
   *         according to the "short scale" naming system (i.e. 1000-based)
   */
  public String word() {
    return word;
  }

}

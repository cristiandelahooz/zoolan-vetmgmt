package com.wornux.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RegexGenerator {

  public RegexGenerator() {}

  public static String generateMatchingString(int length) {
    if (length < 5) throw new IllegalArgumentException("Length must be at least 5");

    Random random = new Random();

    List<Character> chars = new ArrayList<>();

    String all = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    chars.add(randomChar(all));

    chars.add(randomChar("0123456789"));

    chars.add(randomChar("(!@#$%&*()_+.)"));

    while (chars.size() < length) {
      chars.add(randomChar(all));
    }

    Collections.shuffle(chars);

    StringBuilder sb = new StringBuilder();
    for (char c : chars) sb.append(c);

    return sb.toString();
  }

  private static char randomChar(String candidates) {
    return candidates.charAt(new Random().nextInt(candidates.length()));
  }
}

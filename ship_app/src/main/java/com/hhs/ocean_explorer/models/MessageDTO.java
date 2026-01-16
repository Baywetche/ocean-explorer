package com.hhs.ocean_explorer.models;

public class MessageDTO {
  private String text;

  public MessageDTO() {}

  public MessageDTO (String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}

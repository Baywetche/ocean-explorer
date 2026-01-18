package com.hhs.shipapp.models.enums;

public enum NavigableDirections {
    ForwardCenter("n"),
    ForwardRight("ne"),
    BackwardRight("se"),
    BackwardCenter("s"),
    BackwardLeft("sw"),
    ForwardLeft("nw");


    private String key;

    private NavigableDirections(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static String fromString(String navigableDirection) {
        return switch (navigableDirection) {
            case "ForwardCenter" -> NavigableDirections.ForwardCenter.getKey();
            case "ForwardRight" -> NavigableDirections.ForwardRight.getKey();
            case "BackwardRight" -> NavigableDirections.BackwardRight.getKey();
            case "BackwardCenter" -> NavigableDirections.BackwardCenter.getKey();
            case "BackwardLeft" -> NavigableDirections.BackwardLeft.getKey();
            case "ForwardLeft" -> NavigableDirections.ForwardLeft.getKey();

            default -> throw new IllegalStateException("Unexpected navigable direction: " + navigableDirection);
        };
    }
}

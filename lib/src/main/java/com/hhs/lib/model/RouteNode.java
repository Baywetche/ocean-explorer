package com.hhs.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RouteNode {

  private Map<String, RouteNode> children = new HashMap<>();
  private List<Vec2D> routeList = new ArrayList<>();
}


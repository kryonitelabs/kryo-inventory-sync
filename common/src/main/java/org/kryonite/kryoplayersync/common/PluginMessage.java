package org.kryonite.kryoplayersync.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PluginMessage {

  NAMESPACE("kryo-player-sync"),
  SAVE_PLAYER_DATA("save-player-data"),
  INITIAL_JOIN("initial-join");

  private final String value;
}
